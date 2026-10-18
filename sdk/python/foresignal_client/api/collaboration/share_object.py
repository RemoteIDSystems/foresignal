from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.object_acl import ObjectAcl
from ...models.problem import Problem
from ...models.share_request import ShareRequest
from ...types import Response


def _get_kwargs(
    ref: str,
    *,
    body: ShareRequest,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/objects/{ref}/share".format(
            ref=quote(str(ref), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ObjectAcl | Problem | None:
    if response.status_code == 200:
        response_200 = ObjectAcl.from_dict(response.json())

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
) -> Response[ObjectAcl | Problem]:
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
    body: ShareRequest,
) -> Response[ObjectAcl | Problem]:
    """Share an object

     Adds grants for principals, groups or the instance (owner or editor) and notifies the grantees
    (`share.received`). TLP bounds the targets: RED only named principals, AMBER+STRICT the same
    organization, AMBER plus recipients' organizations, GREEN the instance and peers, CLEAR anyone.
    Audited.

    **Scope:** `agent`.

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (ShareRequest): Share an object with principals, groups or the instance; grantees are
            notified

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ObjectAcl | Problem]
    """

    kwargs = _get_kwargs(
        ref=ref,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    ref: str,
    *,
    client: AuthenticatedClient,
    body: ShareRequest,
) -> ObjectAcl | Problem | None:
    """Share an object

     Adds grants for principals, groups or the instance (owner or editor) and notifies the grantees
    (`share.received`). TLP bounds the targets: RED only named principals, AMBER+STRICT the same
    organization, AMBER plus recipients' organizations, GREEN the instance and peers, CLEAR anyone.
    Audited.

    **Scope:** `agent`.

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (ShareRequest): Share an object with principals, groups or the instance; grantees are
            notified

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ObjectAcl | Problem
    """

    return sync_detailed(
        ref=ref,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    ref: str,
    *,
    client: AuthenticatedClient,
    body: ShareRequest,
) -> Response[ObjectAcl | Problem]:
    """Share an object

     Adds grants for principals, groups or the instance (owner or editor) and notifies the grantees
    (`share.received`). TLP bounds the targets: RED only named principals, AMBER+STRICT the same
    organization, AMBER plus recipients' organizations, GREEN the instance and peers, CLEAR anyone.
    Audited.

    **Scope:** `agent`.

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (ShareRequest): Share an object with principals, groups or the instance; grantees are
            notified

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ObjectAcl | Problem]
    """

    kwargs = _get_kwargs(
        ref=ref,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    ref: str,
    *,
    client: AuthenticatedClient,
    body: ShareRequest,
) -> ObjectAcl | Problem | None:
    """Share an object

     Adds grants for principals, groups or the instance (owner or editor) and notifies the grantees
    (`share.received`). TLP bounds the targets: RED only named principals, AMBER+STRICT the same
    organization, AMBER plus recipients' organizations, GREEN the instance and peers, CLEAR anyone.
    Audited.

    **Scope:** `agent`.

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (ShareRequest): Share an object with principals, groups or the instance; grantees are
            notified

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ObjectAcl | Problem
    """

    return (
        await asyncio_detailed(
            ref=ref,
            client=client,
            body=body,
        )
    ).parsed
