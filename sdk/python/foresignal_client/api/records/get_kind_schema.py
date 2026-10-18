from http import HTTPStatus
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.kind_schema import KindSchema
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    kind: str,
    *,
    if_none_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/kinds/{kind}/schema".format(
            kind=quote(str(kind), safe=""),
        ),
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | KindSchema | Problem | None:
    if response.status_code == 200:
        response_200 = KindSchema.from_dict(response.json())

        return response_200

    if response.status_code == 304:
        response_304 = cast(Any, None)
        return response_304

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
) -> Response[Any | KindSchema | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    kind: str,
    *,
    client: AuthenticatedClient,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | KindSchema | Problem]:
    """JSON Schema of a kind's payload

     Merges the fieldSpecs of every source emitting the kind into a JSON Schema 2020-12 document
    (`x-unit`, `x-unitLabel`, `x-label`, `x-format`, `x-display` from kindDisplay); conflicting types
    become `oneOf` with `x-conflicts` listing the source ids. Strong ETag over the merged document.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        kind (str):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | KindSchema | Problem]
    """

    kwargs = _get_kwargs(
        kind=kind,
        if_none_match=if_none_match,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    kind: str,
    *,
    client: AuthenticatedClient,
    if_none_match: str | Unset = UNSET,
) -> Any | KindSchema | Problem | None:
    """JSON Schema of a kind's payload

     Merges the fieldSpecs of every source emitting the kind into a JSON Schema 2020-12 document
    (`x-unit`, `x-unitLabel`, `x-label`, `x-format`, `x-display` from kindDisplay); conflicting types
    become `oneOf` with `x-conflicts` listing the source ids. Strong ETag over the merged document.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        kind (str):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | KindSchema | Problem
    """

    return sync_detailed(
        kind=kind,
        client=client,
        if_none_match=if_none_match,
    ).parsed


async def asyncio_detailed(
    kind: str,
    *,
    client: AuthenticatedClient,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | KindSchema | Problem]:
    """JSON Schema of a kind's payload

     Merges the fieldSpecs of every source emitting the kind into a JSON Schema 2020-12 document
    (`x-unit`, `x-unitLabel`, `x-label`, `x-format`, `x-display` from kindDisplay); conflicting types
    become `oneOf` with `x-conflicts` listing the source ids. Strong ETag over the merged document.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        kind (str):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | KindSchema | Problem]
    """

    kwargs = _get_kwargs(
        kind=kind,
        if_none_match=if_none_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    kind: str,
    *,
    client: AuthenticatedClient,
    if_none_match: str | Unset = UNSET,
) -> Any | KindSchema | Problem | None:
    """JSON Schema of a kind's payload

     Merges the fieldSpecs of every source emitting the kind into a JSON Schema 2020-12 document
    (`x-unit`, `x-unitLabel`, `x-label`, `x-format`, `x-display` from kindDisplay); conflicting types
    become `oneOf` with `x-conflicts` listing the source ids. Strong ETag over the merged document.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        kind (str):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | KindSchema | Problem
    """

    return (
        await asyncio_detailed(
            kind=kind,
            client=client,
            if_none_match=if_none_match,
        )
    ).parsed
