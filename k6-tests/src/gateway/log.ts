import { RefinedResponse, ResponseType } from 'k6/http';

function log(response: RefinedResponse<ResponseType | undefined>) {
  if (response.status >= 400) {
    console.error(
      `Received HTTP ${response.status} for ${response.url}, `
        + `request.body: ${JSON.stringify(response.request.body)}, `
            + `response.body: ${response.body}`,
    );
  }
}

export default log;
