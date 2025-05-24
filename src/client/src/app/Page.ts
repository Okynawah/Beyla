export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  size: number;
  number: number;
}
