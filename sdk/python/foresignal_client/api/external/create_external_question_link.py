from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.external_question_link import ExternalQuestionLink
from ...models.external_question_link_input import ExternalQuestionLinkInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    source_id: str,
    *,
    body: ExternalQuestionLinkInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/external/sources/{source_id}/question-links".format(
            source_id=quote(str(source_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ExternalQuestionLink | Problem | None:
    if response.status_code == 201:
        response_201 = ExternalQuestionLink.from_dict(response.json())

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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ExternalQuestionLink | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: ExternalQuestionLinkInput,
) -> Response[ExternalQuestionLink | Problem]:
    """Link a provider question

     Links a provider question id to a ledger question so its forecast items become external forecasts
    there.

    **Scope:** `agent`.

    Args:
        source_id (str):
        body (ExternalQuestionLinkInput): Map a provider question id to a ledger question (138)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExternalQuestionLink | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: ExternalQuestionLinkInput,
) -> ExternalQuestionLink | Problem | None:
    """Link a provider question

     Links a provider question id to a ledger question so its forecast items become external forecasts
    there.

    **Scope:** `agent`.

    Args:
        source_id (str):
        body (ExternalQuestionLinkInput): Map a provider question id to a ledger question (138)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExternalQuestionLink | Problem
    """

    return sync_detailed(
        source_id=source_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: ExternalQuestionLinkInput,
) -> Response[ExternalQuestionLink | Problem]:
    """Link a provider question

     Links a provider question id to a ledger question so its forecast items become external forecasts
    there.

    **Scope:** `agent`.

    Args:
        source_id (str):
        body (ExternalQuestionLinkInput): Map a provider question id to a ledger question (138)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExternalQuestionLink | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: ExternalQuestionLinkInput,
) -> ExternalQuestionLink | Problem | None:
    """Link a provider question

     Links a provider question id to a ledger question so its forecast items become external forecasts
    there.

    **Scope:** `agent`.

    Args:
        source_id (str):
        body (ExternalQuestionLinkInput): Map a provider question id to a ledger question (138)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExternalQuestionLink | Problem
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            client=client,
            body=body,
        )
    ).parsed
