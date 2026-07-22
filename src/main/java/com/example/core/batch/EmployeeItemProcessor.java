package com.example.core.batch;

import com.example.core.batch.dto.EmployeeCsvRecord;
import com.example.core.employee.EmployeeEntity;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class EmployeeItemProcessor implements ItemProcessor<EmployeeCsvRecord, EmployeeEntity> {

  @Override
  public EmployeeEntity process(EmployeeCsvRecord csvRecord) throws InterruptedException {
    // ⏱️ Artificial delay: Pause for 1 second per item
    // Random delay between 1-3 seconds
    Thread.sleep(1000 * Math.max(1, Math.min(5, (int) (Math.random() * 3))));

    // Filter invalid negative salary records (returning null skips writing this
    // item)
    if (csvRecord.salary() == null || csvRecord.salary() < 0) {
      return null;
    }

    // Business Logic: Capitalize name and prepare entity
    String formattedName = capitalizeWords(csvRecord.name());

    return EmployeeEntity.builder()
        .name(formattedName)
        .salary(csvRecord.salary())
        .createdBy("BATCH_JOB_IMPORT")
        .build();
  }

  private String capitalizeWords(String input) {
    if (input == null || input.isBlank()) return input;
    String[] words = input.split("\\s+");
    StringBuilder sb = new StringBuilder();
    for (String word : words) {
      if (!word.isEmpty()) {
        sb.append(Character.toUpperCase(word.charAt(0)))
            .append(word.substring(1).toLowerCase())
            .append(" ");
      }
    }
    return sb.toString().trim();
  }
}
