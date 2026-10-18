from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.hypothesis_set_page import HypothesisSetPage
from ...models.list_hypothesis_sets_cadence import ListHypothesisSetsCadence
from ...models.list_hypothesis_sets_status import ListHypothesisSetsStatus
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    status: ListHypothesisSetsStatus | Unset = UNSET,
    cadence: ListHypothesisSetsCadence | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    region: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_status: str | Unset = UNSET
    if not isinstance(status, Unset):
        json_status = status.value

    params["status"] = json_status

    json_cadence: str | Unset = UNSET
    if not isinstance(cadence, Unset):
        json_cadence = cadence.value

    params["cadence"] = json_cadence

    params["situationId"] = situation_id

    params["region"] = region

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/hypotheses/sets",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> HypothesisSetPage | Problem | None:
    if response.status_code == 200:
        response_200 = HypothesisSetPage.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[HypothesisSetPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    status: ListHypothesisSetsStatus | Unset = UNSET,
    cadence: ListHypothesisSetsCadence | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    region: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[HypothesisSetPage | Problem]:
    """List hypothesis sets

     Sets with their hypotheses' probabilities, ICD terms and confidence; private sets only for callers
    allowed private data.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (ListHypothesisSetsStatus | Unset):
        cadence (ListHypothesisSetsCadence | Unset):
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        region (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[HypothesisSetPage | Problem]
    """

    kwargs = _get_kwargs(
        status=status,
        cadence=cadence,
        situation_id=situation_id,
        region=region,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    status: ListHypothesisSetsStatus | Unset = UNSET,
    cadence: ListHypothesisSetsCadence | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    region: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> HypothesisSetPage | Problem | None:
    """List hypothesis sets

     Sets with their hypotheses' probabilities, ICD terms and confidence; private sets only for callers
    allowed private data.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (ListHypothesisSetsStatus | Unset):
        cadence (ListHypothesisSetsCadence | Unset):
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        region (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        HypothesisSetPage | Problem
    """

    return sync_detailed(
        client=client,
        status=status,
        cadence=cadence,
        situation_id=situation_id,
        region=region,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    status: ListHypothesisSetsStatus | Unset = UNSET,
    cadence: ListHypothesisSetsCadence | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    region: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[HypothesisSetPage | Problem]:
    """List hypothesis sets

     Sets with their hypotheses' probabilities, ICD terms and confidence; private sets only for callers
    allowed private data.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (ListHypothesisSetsStatus | Unset):
        cadence (ListHypothesisSetsCadence | Unset):
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        region (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[HypothesisSetPage | Problem]
    """

    kwargs = _get_kwargs(
        status=status,
        cadence=cadence,
        situation_id=situation_id,
        region=region,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    status: ListHypothesisSetsStatus | Unset = UNSET,
    cadence: ListHypothesisSetsCadence | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    region: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> HypothesisSetPage | Problem | None:
    """List hypothesis sets

     Sets with their hypotheses' probabilities, ICD terms and confidence; private sets only for callers
    allowed private data.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (ListHypothesisSetsStatus | Unset):
        cadence (ListHypothesisSetsCadence | Unset):
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        region (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        HypothesisSetPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            status=status,
            cadence=cadence,
            situation_id=situation_id,
            region=region,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
