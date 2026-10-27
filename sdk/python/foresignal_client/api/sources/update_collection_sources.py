from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.collection_set import CollectionSet
from ...models.collection_set_update import CollectionSetUpdate
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: CollectionSetUpdate,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/sources/collection",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> CollectionSet | Problem | None:
    if response.status_code == 200:
        response_200 = CollectionSet.from_dict(response.json())

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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[CollectionSet | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: CollectionSetUpdate,
) -> Response[CollectionSet | Problem]:
    """Update the instance collection set

     Sets `inCollection` for every listed source, all or nothing; a source left out keeps its state and
    `enabledByUsers` is ignored. Each change applies the checks of updateSource, so collecting a source
    without its required credentials is `409 CREDENTIALS_REQUIRED`. A source taken out of the set is
    pulled for no one, regardless of per-user views. Requires `collection.manage`.

    **Scope:** `admin`.

    Args:
        body (CollectionSetUpdate): Replace the instance collection set

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CollectionSet | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: CollectionSetUpdate,
) -> CollectionSet | Problem | None:
    """Update the instance collection set

     Sets `inCollection` for every listed source, all or nothing; a source left out keeps its state and
    `enabledByUsers` is ignored. Each change applies the checks of updateSource, so collecting a source
    without its required credentials is `409 CREDENTIALS_REQUIRED`. A source taken out of the set is
    pulled for no one, regardless of per-user views. Requires `collection.manage`.

    **Scope:** `admin`.

    Args:
        body (CollectionSetUpdate): Replace the instance collection set

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CollectionSet | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: CollectionSetUpdate,
) -> Response[CollectionSet | Problem]:
    """Update the instance collection set

     Sets `inCollection` for every listed source, all or nothing; a source left out keeps its state and
    `enabledByUsers` is ignored. Each change applies the checks of updateSource, so collecting a source
    without its required credentials is `409 CREDENTIALS_REQUIRED`. A source taken out of the set is
    pulled for no one, regardless of per-user views. Requires `collection.manage`.

    **Scope:** `admin`.

    Args:
        body (CollectionSetUpdate): Replace the instance collection set

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CollectionSet | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: CollectionSetUpdate,
) -> CollectionSet | Problem | None:
    """Update the instance collection set

     Sets `inCollection` for every listed source, all or nothing; a source left out keeps its state and
    `enabledByUsers` is ignored. Each change applies the checks of updateSource, so collecting a source
    without its required credentials is `409 CREDENTIALS_REQUIRED`. A source taken out of the set is
    pulled for no one, regardless of per-user views. Requires `collection.manage`.

    **Scope:** `admin`.

    Args:
        body (CollectionSetUpdate): Replace the instance collection set

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CollectionSet | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
