from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.situation import Situation
from ...models.situation_merge_input import SituationMergeInput
from ...types import Response


def _get_kwargs(
    situation_id: str,
    *,
    body: SituationMergeInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/situations/{situation_id}/merge".format(
            situation_id=quote(str(situation_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | Situation | None:
    if response.status_code == 200:
        response_200 = Situation.from_dict(response.json())

        return response_200

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
) -> Response[Problem | Situation]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    situation_id: str,
    *,
    client: AuthenticatedClient,
    body: SituationMergeInput,
) -> Response[Problem | Situation]:
    """Merge two situations

     Merges this situation with `intoId`: the older of the two keeps its id and receives the members, the
    other gets `mergedInto` and its `foresignal-intel` record is tombstoned; both logs record the merge
    and `situation.merged` is emitted. Returns the surviving situation.

    **Scope:** `agent` or `admin`.

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (SituationMergeInput): Merge this situation with another; the older id survives

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Situation]
    """

    kwargs = _get_kwargs(
        situation_id=situation_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    situation_id: str,
    *,
    client: AuthenticatedClient,
    body: SituationMergeInput,
) -> Problem | Situation | None:
    """Merge two situations

     Merges this situation with `intoId`: the older of the two keeps its id and receives the members, the
    other gets `mergedInto` and its `foresignal-intel` record is tombstoned; both logs record the merge
    and `situation.merged` is emitted. Returns the surviving situation.

    **Scope:** `agent` or `admin`.

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (SituationMergeInput): Merge this situation with another; the older id survives

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Situation
    """

    return sync_detailed(
        situation_id=situation_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    situation_id: str,
    *,
    client: AuthenticatedClient,
    body: SituationMergeInput,
) -> Response[Problem | Situation]:
    """Merge two situations

     Merges this situation with `intoId`: the older of the two keeps its id and receives the members, the
    other gets `mergedInto` and its `foresignal-intel` record is tombstoned; both logs record the merge
    and `situation.merged` is emitted. Returns the surviving situation.

    **Scope:** `agent` or `admin`.

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (SituationMergeInput): Merge this situation with another; the older id survives

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Situation]
    """

    kwargs = _get_kwargs(
        situation_id=situation_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    situation_id: str,
    *,
    client: AuthenticatedClient,
    body: SituationMergeInput,
) -> Problem | Situation | None:
    """Merge two situations

     Merges this situation with `intoId`: the older of the two keeps its id and receives the members, the
    other gets `mergedInto` and its `foresignal-intel` record is tombstoned; both logs record the merge
    and `situation.merged` is emitted. Returns the surviving situation.

    **Scope:** `agent` or `admin`.

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (SituationMergeInput): Merge this situation with another; the older id survives

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Situation
    """

    return (
        await asyncio_detailed(
            situation_id=situation_id,
            client=client,
            body=body,
        )
    ).parsed
