from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.annotation import Annotation
from ...models.annotation_thread import AnnotationThread
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    ref: str,
    *,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
    thread: bool | Unset = False,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["cursor"] = cursor

    params["limit"] = limit

    params["thread"] = thread

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/objects/{ref}/annotations".format(
            ref=quote(str(ref), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | list[Annotation] | list[AnnotationThread] | None:
    if response.status_code == 200:

        def _parse_response_200(
            data: object,
        ) -> list[Annotation] | list[AnnotationThread]:
            try:
                if not isinstance(data, list):
                    raise TypeError()
                response_200_type_0 = []
                _response_200_type_0 = data
                for response_200_type_0_item_data in _response_200_type_0:
                    response_200_type_0_item = Annotation.from_dict(
                        response_200_type_0_item_data
                    )

                    response_200_type_0.append(response_200_type_0_item)

                return response_200_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, list):
                raise TypeError()
            response_200_type_1 = []
            _response_200_type_1 = data
            for response_200_type_1_item_data in _response_200_type_1:
                response_200_type_1_item = AnnotationThread.from_dict(
                    response_200_type_1_item_data
                )

                response_200_type_1.append(response_200_type_1_item)

            return response_200_type_1

        response_200 = _parse_response_200(response.json())

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | list[Annotation] | list[AnnotationThread]]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    ref: str,
    *,
    client: AuthenticatedClient,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
    thread: bool | Unset = False,
) -> Response[Problem | list[Annotation] | list[AnnotationThread]]:
    """List an object's annotations

     Notes and verdicts on the object visible to the caller.

    **Scope:** `read` (token required).

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.
        thread (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | list[Annotation] | list[AnnotationThread]]
    """

    kwargs = _get_kwargs(
        ref=ref,
        cursor=cursor,
        limit=limit,
        thread=thread,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    ref: str,
    *,
    client: AuthenticatedClient,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
    thread: bool | Unset = False,
) -> Problem | list[Annotation] | list[AnnotationThread] | None:
    """List an object's annotations

     Notes and verdicts on the object visible to the caller.

    **Scope:** `read` (token required).

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.
        thread (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | list[Annotation] | list[AnnotationThread]
    """

    return sync_detailed(
        ref=ref,
        client=client,
        cursor=cursor,
        limit=limit,
        thread=thread,
    ).parsed


async def asyncio_detailed(
    ref: str,
    *,
    client: AuthenticatedClient,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
    thread: bool | Unset = False,
) -> Response[Problem | list[Annotation] | list[AnnotationThread]]:
    """List an object's annotations

     Notes and verdicts on the object visible to the caller.

    **Scope:** `read` (token required).

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.
        thread (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | list[Annotation] | list[AnnotationThread]]
    """

    kwargs = _get_kwargs(
        ref=ref,
        cursor=cursor,
        limit=limit,
        thread=thread,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    ref: str,
    *,
    client: AuthenticatedClient,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
    thread: bool | Unset = False,
) -> Problem | list[Annotation] | list[AnnotationThread] | None:
    """List an object's annotations

     Notes and verdicts on the object visible to the caller.

    **Scope:** `read` (token required).

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.
        thread (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | list[Annotation] | list[AnnotationThread]
    """

    return (
        await asyncio_detailed(
            ref=ref,
            client=client,
            cursor=cursor,
            limit=limit,
            thread=thread,
        )
    ).parsed
