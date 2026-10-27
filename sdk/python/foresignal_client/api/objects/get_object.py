import datetime
from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.object_envelope import ObjectEnvelope
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    ref: str,
    *,
    as_of: datetime.datetime | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_as_of: str | Unset = UNSET
    if not isinstance(as_of, Unset):
        json_as_of = as_of.isoformat()
    params["asOf"] = json_as_of

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/objects/{ref}".format(
            ref=quote(str(ref), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ObjectEnvelope | Problem | None:
    if response.status_code == 200:
        response_200 = ObjectEnvelope.from_dict(response.json())

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
) -> Response[ObjectEnvelope | Problem]:
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
    as_of: datetime.datetime | Unset = UNSET,
) -> Response[ObjectEnvelope | Problem]:
    """Resolve one object reference

     Returns the uniform envelope, `asOf`-aware, with `actions` the calling token may perform. Unknown
    types → 400 `OBJECT_REF_INVALID`; private or unshared objects are invisible (404).

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        as_of (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ObjectEnvelope | Problem]
    """

    kwargs = _get_kwargs(
        ref=ref,
        as_of=as_of,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    ref: str,
    *,
    client: AuthenticatedClient,
    as_of: datetime.datetime | Unset = UNSET,
) -> ObjectEnvelope | Problem | None:
    """Resolve one object reference

     Returns the uniform envelope, `asOf`-aware, with `actions` the calling token may perform. Unknown
    types → 400 `OBJECT_REF_INVALID`; private or unshared objects are invisible (404).

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        as_of (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ObjectEnvelope | Problem
    """

    return sync_detailed(
        ref=ref,
        client=client,
        as_of=as_of,
    ).parsed


async def asyncio_detailed(
    ref: str,
    *,
    client: AuthenticatedClient,
    as_of: datetime.datetime | Unset = UNSET,
) -> Response[ObjectEnvelope | Problem]:
    """Resolve one object reference

     Returns the uniform envelope, `asOf`-aware, with `actions` the calling token may perform. Unknown
    types → 400 `OBJECT_REF_INVALID`; private or unshared objects are invisible (404).

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        as_of (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ObjectEnvelope | Problem]
    """

    kwargs = _get_kwargs(
        ref=ref,
        as_of=as_of,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    ref: str,
    *,
    client: AuthenticatedClient,
    as_of: datetime.datetime | Unset = UNSET,
) -> ObjectEnvelope | Problem | None:
    """Resolve one object reference

     Returns the uniform envelope, `asOf`-aware, with `actions` the calling token may perform. Unknown
    types → 400 `OBJECT_REF_INVALID`; private or unshared objects are invisible (404).

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        as_of (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ObjectEnvelope | Problem
    """

    return (
        await asyncio_detailed(
            ref=ref,
            client=client,
            as_of=as_of,
        )
    ).parsed
