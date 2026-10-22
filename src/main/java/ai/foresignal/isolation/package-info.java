/**
 * Keeps the API answering whatever the data sources are doing: the roles a thread can work in
 * ({@link ai.foresignal.isolation.WorkRole}), dedicated platform threads for data gathering
 * ({@link ai.foresignal.isolation.WorkThreads}), and a separate database pool per role
 * ({@link ai.foresignal.isolation.RoleRoutingDataSource}).
 */
package ai.foresignal.isolation;
