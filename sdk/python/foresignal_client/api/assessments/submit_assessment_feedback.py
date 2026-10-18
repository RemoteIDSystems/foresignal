from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.assessment_feedback import AssessmentFeedback
from ...models.assessment_feedback_input import AssessmentFeedbackInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    assessment_id: str,
    *,
    body: AssessmentFeedbackInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/assessments/{assessment_id}/feedback".format(
            assessment_id=quote(str(assessment_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AssessmentFeedback | Problem | None:
    if response.status_code == 201:
        response_201 = AssessmentFeedback.from_dict(response.json())

        return response_201

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[AssessmentFeedback | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    assessment_id: str,
    *,
    client: AuthenticatedClient,
    body: AssessmentFeedbackInput,
) -> Response[AssessmentFeedback | Problem]:
    """Rate an assessment

     Records `useful`, `not_useful` or `wrong` into 102's analyst feedback.

    **Scope:** `read` (token required).

    Args:
        assessment_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AssessmentFeedbackInput): Rating of an assessment (stored as 102 analyst feedback)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AssessmentFeedback | Problem]
    """

    kwargs = _get_kwargs(
        assessment_id=assessment_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    assessment_id: str,
    *,
    client: AuthenticatedClient,
    body: AssessmentFeedbackInput,
) -> AssessmentFeedback | Problem | None:
    """Rate an assessment

     Records `useful`, `not_useful` or `wrong` into 102's analyst feedback.

    **Scope:** `read` (token required).

    Args:
        assessment_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AssessmentFeedbackInput): Rating of an assessment (stored as 102 analyst feedback)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AssessmentFeedback | Problem
    """

    return sync_detailed(
        assessment_id=assessment_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    assessment_id: str,
    *,
    client: AuthenticatedClient,
    body: AssessmentFeedbackInput,
) -> Response[AssessmentFeedback | Problem]:
    """Rate an assessment

     Records `useful`, `not_useful` or `wrong` into 102's analyst feedback.

    **Scope:** `read` (token required).

    Args:
        assessment_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AssessmentFeedbackInput): Rating of an assessment (stored as 102 analyst feedback)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AssessmentFeedback | Problem]
    """

    kwargs = _get_kwargs(
        assessment_id=assessment_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    assessment_id: str,
    *,
    client: AuthenticatedClient,
    body: AssessmentFeedbackInput,
) -> AssessmentFeedback | Problem | None:
    """Rate an assessment

     Records `useful`, `not_useful` or `wrong` into 102's analyst feedback.

    **Scope:** `read` (token required).

    Args:
        assessment_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AssessmentFeedbackInput): Rating of an assessment (stored as 102 analyst feedback)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AssessmentFeedback | Problem
    """

    return (
        await asyncio_detailed(
            assessment_id=assessment_id,
            client=client,
            body=body,
        )
    ).parsed
