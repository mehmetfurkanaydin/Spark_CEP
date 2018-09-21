# Spark_CEP
Complex event processing with Spark Streaming

1 - User interactions are retrieved as event log data <br />
2 - Event logs are loaded to Kafka Producer<br />
3 - Data is loaded to Spark Streaming from Kafka Consumer<br />
4 - In the Spark Streaming Rule Detection and Collaborative Filtering are used for analyzing this data <br />
6 - As a result of streaming data(alerts) is loaded to Kafka producer<br />
7 - Alerts are retrieved from Kafka Consumer.<br />
8 - Alerts are sent to the user via email and SMS. And also alerts are shown on GUI.<br />
9 - Alerts are loaded to Elasticsearch for make them searchable and visualizable.<br />
