package org.molgenis.promise.job;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import org.molgenis.data.DataService;
import org.molgenis.jobs.Job;
import org.molgenis.jobs.JobFactory;
import org.molgenis.jobs.model.ScheduledJobType;
import org.molgenis.jobs.model.ScheduledJobTypeFactory;
import org.molgenis.promise.PromiseMapperType;
import org.molgenis.promise.mapper.PromiseMapper;
import org.molgenis.promise.mapper.PromiseMapperFactory;
import org.molgenis.promise.model.PromiseCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Objects.requireNonNull;
import static org.molgenis.promise.PromiseMapperType.PAREL;
import static org.molgenis.promise.PromiseMapperType.RADBOUD;
import static org.molgenis.promise.model.PromiseCredentialsMetadata.PROMISE_CREDENTIALS;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Configuration
public class PromiseJobConfig
{
	private final Gson gson;
	private final ScheduledJobTypeFactory scheduledJobTypeFactory;
	private final PromiseJobExecutionMetadata promiseJobExecutionMetadata;
	private final PromiseMapperFactory promiseMapperFactory;
	private final DataService dataService;

	public PromiseJobConfig(ScheduledJobTypeFactory scheduledJobTypeFactory,
			PromiseJobExecutionMetadata promiseJobExecutionMetadata, PromiseMapperFactory promiseMapperFactory,
			DataService dataService)
	{
		this.scheduledJobTypeFactory = requireNonNull(scheduledJobTypeFactory);
		this.promiseJobExecutionMetadata = requireNonNull(promiseJobExecutionMetadata);
		this.promiseMapperFactory = requireNonNull(promiseMapperFactory);
		this.dataService = requireNonNull(dataService);
		this.gson = new Gson();
	}

	@Bean
	public JobFactory<PromiseJobExecution> promiseJobFactory()
	{
		return new JobFactory<PromiseJobExecution>()
		{
			@Override
			public Job createJob(PromiseJobExecution promiseJobExecution)
			{
				final String biobankId = promiseJobExecution.getBiobankId();
				final PromiseMapperType mapperType = promiseJobExecution.getMapper();
				PromiseCredentials credentials = dataService.findOneById(PROMISE_CREDENTIALS,
						promiseJobExecution.getCredentials(), PromiseCredentials.class);
				PromiseMapper mapper = promiseMapperFactory.getMapper(mapperType);

				return progress -> mapper.map(progress, credentials, biobankId);
			}
		};
	}

	@Lazy
	@Bean
	@SuppressWarnings("squid:S1192")
	public ScheduledJobType promiseJobType()
	{
		ScheduledJobType result = scheduledJobTypeFactory.create(PromiseJobExecutionMetadata.JOB_TYPE);
		result.setLabel("ProMISe");
		result.setDescription("Job that translates ProMISe biobank entities to BBMRI sample collection entities");
		result.setSchema(gson.toJson(of("title", "Promise Job", "type", "object", "properties",
				of("biobankId", of("type", "string", "description", "The identifier of the target biobank."), "mapper",
						of("type", "string", "description", "The Promise Mapper Type to use.", "enum",
								ImmutableList.of(PAREL, RADBOUD)), "credentials",
						of("type", "string", "description", "The identifier of the PromiseCredentials entity to use")),
				"required", ImmutableList.of("biobankId", "mapper", "credentials"))));
		result.setJobExecutionType(promiseJobExecutionMetadata);
		return result;
	}
}
