package liquibase.dsl.parser.groovy

import liquibase.preconditions.Preconditions

public interface ConditionallyExecuted {
  public Preconditions getPreconditions()
  public void setPreconditions(Preconditions preconditions)
}