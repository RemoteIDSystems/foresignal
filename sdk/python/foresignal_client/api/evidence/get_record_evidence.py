from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.record_evidence import RecordEvidence
from ...types import UNSET, Response


def _get_kwargs(
    *,
    source: str,
    external_id: str,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["source"] = source

    params["externalId"] = external_id

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/evidence/of",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | RecordEvidence | None:
    if response.status_code == 200:
        response_200 = RecordEvidence.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

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
) -> Response[Problem | RecordEvidence]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    source: str,
    external_id: str,
) -> Response[Problem | RecordEvidence]:
    """Evidence identity of a record

     The record's originator, its observation links and the canonical events it belongs to.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (str):
        external_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RecordEvidence]
    """

    kwargs = _get_kwargs(
        source=source,
        external_id=external_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    source: str,
    external_id: str,
) -> Problem | RecordEvidence | None:
    """Evidence identity of a record

     The record's originator, its observation links and the canonical events it belongs to.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (str):
        external_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RecordEvidence
    """

    return sync_detailed(
        client=client,
        source=source,
        external_id=external_id,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    source: str,
    external_id: str,
) -> Response[Problem | RecordEvidence]:
    """Evidence identity of a record

     The record's originator, its observation links and the canonical events it belongs to.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (str):
        external_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RecordEvidence]
    """

    kwargs = _get_kwargs(
        source=source,
        external_id=external_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    source: str,
    external_id: str,
) -> Problem | RecordEvidence | None:
    """Evidence identity of a record

     The record's originator, its observation links and the canonical events it belongs to.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (str):
        external_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RecordEvidence
    """

    return (
        await asyncio_detailed(
            client=client,
            source=source,
            external_id=external_id,
        )
    ).parsed
