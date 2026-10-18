import datetime
from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.taxii_envelope import TaxiiEnvelope
from ...types import UNSET, Response, Unset


def _get_kwargs(
    collection_id: str,
    *,
    added_after: datetime.datetime | Unset = UNSET,
    limit: int | Unset = UNSET,
    next_: str | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_added_after: str | Unset = UNSET
    if not isinstance(added_after, Unset):
        json_added_after = added_after.isoformat()
    params["added_after"] = json_added_after

    params["limit"] = limit

    params["next"] = next_

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/taxii2/foresignal/collections/{collection_id}/objects/".format(
            collection_id=quote(str(collection_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | TaxiiEnvelope | None:
    if response.status_code == 200:
        response_200 = TaxiiEnvelope.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | TaxiiEnvelope]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    collection_id: str,
    *,
    client: AuthenticatedClient,
    added_after: datetime.datetime | Unset = UNSET,
    limit: int | Unset = UNSET,
    next_: str | Unset = UNSET,
) -> Response[Problem | TaxiiEnvelope]:
    """TAXII collection objects

     STIX object versions in ingest order with `added_after` (strictly after the version's ingest time)
    and `next` pagination (repeat the same `added_after` with `next`); the default page is 100;
    `X-TAXII-Date-Added-First`/`-Last` headers.

    **Scope:** `agent`.

    Args:
        collection_id (str):
        added_after (datetime.datetime | Unset):
        limit (int | Unset):
        next_ (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | TaxiiEnvelope]
    """

    kwargs = _get_kwargs(
        collection_id=collection_id,
        added_after=added_after,
        limit=limit,
        next_=next_,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    collection_id: str,
    *,
    client: AuthenticatedClient,
    added_after: datetime.datetime | Unset = UNSET,
    limit: int | Unset = UNSET,
    next_: str | Unset = UNSET,
) -> Problem | TaxiiEnvelope | None:
    """TAXII collection objects

     STIX object versions in ingest order with `added_after` (strictly after the version's ingest time)
    and `next` pagination (repeat the same `added_after` with `next`); the default page is 100;
    `X-TAXII-Date-Added-First`/`-Last` headers.

    **Scope:** `agent`.

    Args:
        collection_id (str):
        added_after (datetime.datetime | Unset):
        limit (int | Unset):
        next_ (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | TaxiiEnvelope
    """

    return sync_detailed(
        collection_id=collection_id,
        client=client,
        added_after=added_after,
        limit=limit,
        next_=next_,
    ).parsed


async def asyncio_detailed(
    collection_id: str,
    *,
    client: AuthenticatedClient,
    added_after: datetime.datetime | Unset = UNSET,
    limit: int | Unset = UNSET,
    next_: str | Unset = UNSET,
) -> Response[Problem | TaxiiEnvelope]:
    """TAXII collection objects

     STIX object versions in ingest order with `added_after` (strictly after the version's ingest time)
    and `next` pagination (repeat the same `added_after` with `next`); the default page is 100;
    `X-TAXII-Date-Added-First`/`-Last` headers.

    **Scope:** `agent`.

    Args:
        collection_id (str):
        added_after (datetime.datetime | Unset):
        limit (int | Unset):
        next_ (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | TaxiiEnvelope]
    """

    kwargs = _get_kwargs(
        collection_id=collection_id,
        added_after=added_after,
        limit=limit,
        next_=next_,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    collection_id: str,
    *,
    client: AuthenticatedClient,
    added_after: datetime.datetime | Unset = UNSET,
    limit: int | Unset = UNSET,
    next_: str | Unset = UNSET,
) -> Problem | TaxiiEnvelope | None:
    """TAXII collection objects

     STIX object versions in ingest order with `added_after` (strictly after the version's ingest time)
    and `next` pagination (repeat the same `added_after` with `next`); the default page is 100;
    `X-TAXII-Date-Added-First`/`-Last` headers.

    **Scope:** `agent`.

    Args:
        collection_id (str):
        added_after (datetime.datetime | Unset):
        limit (int | Unset):
        next_ (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | TaxiiEnvelope
    """

    return (
        await asyncio_detailed(
            collection_id=collection_id,
            client=client,
            added_after=added_after,
            limit=limit,
            next_=next_,
        )
    ).parsed
