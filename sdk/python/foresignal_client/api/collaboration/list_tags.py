from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.list_tags_namespace import ListTagsNamespace
from ...models.problem import Problem
from ...models.tag_page import TagPage
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    namespace: ListTagsNamespace | Unset = UNSET,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_namespace: str | Unset = UNSET
    if not isinstance(namespace, Unset):
        json_namespace = namespace.value

    params["namespace"] = json_namespace

    params["q"] = q

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/tags",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | TagPage | None:
    if response.status_code == 200:
        response_200 = TagPage.from_dict(response.json())

        return response_200

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
) -> Response[Problem | TagPage]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    namespace: ListTagsNamespace | Unset = UNSET,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | TagPage]:
    """List tags

     Shared tags visible to the caller plus the caller's personal tags.

    **Scope:** `read`.

    Args:
        namespace (ListTagsNamespace | Unset):
        q (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | TagPage]
    """

    kwargs = _get_kwargs(
        namespace=namespace,
        q=q,
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
    namespace: ListTagsNamespace | Unset = UNSET,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | TagPage | None:
    """List tags

     Shared tags visible to the caller plus the caller's personal tags.

    **Scope:** `read`.

    Args:
        namespace (ListTagsNamespace | Unset):
        q (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | TagPage
    """

    return sync_detailed(
        client=client,
        namespace=namespace,
        q=q,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    namespace: ListTagsNamespace | Unset = UNSET,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | TagPage]:
    """List tags

     Shared tags visible to the caller plus the caller's personal tags.

    **Scope:** `read`.

    Args:
        namespace (ListTagsNamespace | Unset):
        q (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | TagPage]
    """

    kwargs = _get_kwargs(
        namespace=namespace,
        q=q,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    namespace: ListTagsNamespace | Unset = UNSET,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | TagPage | None:
    """List tags

     Shared tags visible to the caller plus the caller's personal tags.

    **Scope:** `read`.

    Args:
        namespace (ListTagsNamespace | Unset):
        q (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | TagPage
    """

    return (
        await asyncio_detailed(
            client=client,
            namespace=namespace,
            q=q,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
