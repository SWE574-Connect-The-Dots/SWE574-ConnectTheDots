/**
 * Formats a date string into dd.MM.YYYY format
 * @param {string} dateString - The date string to format
 * @returns {string} The formatted date string in dd.MM.YYYY format
 */
export const formatDate = (dateString) => {
  const date = new Date(dateString);
  const day = String(date.getDate()).padStart(2, "0");
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const year = date.getFullYear();
  return `${day}.${month}.${year}`;
};
