import { formatDate } from "../utils/dateUtils";

describe("dateUtils", () => {
  describe("formatDate function", () => {
    it("formats ISO date strings correctly", () => {
      const isoDate = "2025-05-08T12:58:27.408592Z";
      const formattedDate = formatDate(isoDate);

      expect(formattedDate).toBe("08.05.2025");
    });

    it("formats YYYY-MM-DD date strings correctly", () => {
      const simpleDate = "1990-07-15";
      const formattedDate = formatDate(simpleDate);

      expect(formattedDate).toBe("15.07.1990");
    });

    it("adds leading zeros to single-digit days and months", () => {
      const dateWithSingleDigits = "2023-1-5";
      const formattedDate = formatDate(dateWithSingleDigits);

      expect(formattedDate).toBe("05.01.2023");
    });

    it("correctly handles dates at month/year boundaries", () => {
      const yearEnd = "2024-12-31";
      expect(formatDate(yearEnd)).toBe("31.12.2024");

      const yearStart = "2024-01-01";
      expect(formatDate(yearStart)).toBe("01.01.2024");
    });

    it("maintains the same format for different date inputs", () => {
      const dates = [
        "2020-01-01",
        "1999-12-31",
        "2025-05-08T12:58:27.408592Z",
        "2022-07-04T00:00:00Z",
      ];

      const datePattern = /^\d{2}\.\d{2}\.\d{4}$/;

      dates.forEach((date) => {
        const formatted = formatDate(date);
        expect(datePattern.test(formatted)).toBe(true);
      });
    });

    it("creates dates that are visually distinguishable from ISO format", () => {
      const isoDate = "2025-05-08T12:58:27.408592Z";
      const formattedDate = formatDate(isoDate);

      expect(formattedDate).not.toContain("-");

      expect(formattedDate).toContain(".");
    });
  });
});
