from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.ogc_collection import OgcCollection
from ...models.ogc_coverage_collection import OgcCoverageCollection
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    collection_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/ogc/collections/{collection_id}".format(
            collection_id=quote(str(collection_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> OgcCollection | OgcCoverageCollection | Problem | None:
    if response.status_code == 200:

        def _parse_response_200(data: object) -> OgcCollection | OgcCoverageCollection:
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                response_200_type_0 = OgcCollection.from_dict(data)

                return response_200_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            response_200_type_1 = OgcCoverageCollection.from_dict(data)

            return response_200_type_1

        response_200 = _parse_response_200(response.json())

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
) -> Response[OgcCollection | OgcCoverageCollection | Problem]:
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
) -> Response[OgcCollection | OgcCoverageCollection | Problem]:
    """Get an OGC API collection

     A product collection (extents, `data_queries` for position, area and instances, parameter names, map
    tiles links) or, from 112, a feature collection.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        collection_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[OgcCollection | OgcCoverageCollection | Problem]
    """

    kwargs = _get_kwargs(
        collection_id=collection_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    collection_id: str,
    *,
    client: AuthenticatedClient,
) -> OgcCollection | OgcCoverageCollection | Problem | None:
    """Get an OGC API collection

     A product collection (extents, `data_queries` for position, area and instances, parameter names, map
    tiles links) or, from 112, a feature collection.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        collection_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        OgcCollection | OgcCoverageCollection | Problem
    """

    return sync_detailed(
        collection_id=collection_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    collection_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[OgcCollection | OgcCoverageCollection | Problem]:
    """Get an OGC API collection

     A product collection (extents, `data_queries` for position, area and instances, parameter names, map
    tiles links) or, from 112, a feature collection.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        collection_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[OgcCollection | OgcCoverageCollection | Problem]
    """

    kwargs = _get_kwargs(
        collection_id=collection_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    collection_id: str,
    *,
    client: AuthenticatedClient,
) -> OgcCollection | OgcCoverageCollection | Problem | None:
    """Get an OGC API collection

     A product collection (extents, `data_queries` for position, area and instances, parameter names, map
    tiles links) or, from 112, a feature collection.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        collection_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        OgcCollection | OgcCoverageCollection | Problem
    """

    return (
        await asyncio_detailed(
            collection_id=collection_id,
            client=client,
        )
    ).parsed
