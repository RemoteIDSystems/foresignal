from http import HTTPStatus
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_record_format import GetRecordFormat
from ...models.get_record_include_item import GetRecordIncludeItem
from ...models.get_record_licence_policy_item import GetRecordLicencePolicyItem
from ...models.problem import Problem
from ...models.record import Record
from ...types import UNSET, Response, Unset


def _get_kwargs(
    source_id: str,
    external_id: str,
    *,
    format_: GetRecordFormat | Unset = GetRecordFormat.JSON,
    include: list[GetRecordIncludeItem] | Unset = UNSET,
    fields: str | Unset = UNSET,
    precision: int | Unset = 7,
    licence_policy: list[GetRecordLicencePolicyItem] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    if not isinstance(foresignal_licence_policy, Unset):
        headers["Foresignal-Licence-Policy"] = foresignal_licence_policy

    params: dict[str, Any] = {}

    json_format_: str | Unset = UNSET
    if not isinstance(format_, Unset):
        json_format_ = format_.value

    params["format"] = json_format_

    json_include: list[str] | Unset = UNSET
    if not isinstance(include, Unset):
        json_include = []
        for include_item_data in include:
            include_item = include_item_data.value
            json_include.append(include_item)

    params["include"] = json_include

    params["fields"] = fields

    params["precision"] = precision

    json_licence_policy: list[str] | Unset = UNSET
    if not isinstance(licence_policy, Unset):
        json_licence_policy = []
        for licence_policy_item_data in licence_policy:
            licence_policy_item = licence_policy_item_data.value
            json_licence_policy.append(licence_policy_item)

    params["licencePolicy"] = json_licence_policy

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/records/{source_id}/{external_id}".format(
            source_id=quote(str(source_id), safe=""),
            external_id=quote(str(external_id), safe=""),
        ),
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | Record | None:
    if response.status_code == 200:
        response_200 = Record.from_dict(response.json())

        return response_200

    if response.status_code == 304:
        response_304 = cast(Any, None)
        return response_304

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
) -> Response[Any | Problem | Record]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    external_id: str,
    *,
    client: AuthenticatedClient,
    format_: GetRecordFormat | Unset = GetRecordFormat.JSON,
    include: list[GetRecordIncludeItem] | Unset = UNSET,
    fields: str | Unset = UNSET,
    precision: int | Unset = 7,
    licence_policy: list[GetRecordLicencePolicyItem] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[Any | Problem | Record]:
    """Get current version of a record

     Current state of one record (head plus live). Strong ETag `"<version>.<observedAt epoch ms>"`;
    `include=observer` reads observers from the head's version when gated in.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):
        external_id (str):
        format_ (GetRecordFormat | Unset):  Default: GetRecordFormat.JSON.
        include (list[GetRecordIncludeItem] | Unset):
        fields (str | Unset):  Example: externalId,title,geometry,payload.mag.
        precision (int | Unset):  Default: 7.
        licence_policy (list[GetRecordLicencePolicyItem] | Unset):
        if_none_match (str | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | Record]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        external_id=external_id,
        format_=format_,
        include=include,
        fields=fields,
        precision=precision,
        licence_policy=licence_policy,
        if_none_match=if_none_match,
        foresignal_licence_policy=foresignal_licence_policy,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    external_id: str,
    *,
    client: AuthenticatedClient,
    format_: GetRecordFormat | Unset = GetRecordFormat.JSON,
    include: list[GetRecordIncludeItem] | Unset = UNSET,
    fields: str | Unset = UNSET,
    precision: int | Unset = 7,
    licence_policy: list[GetRecordLicencePolicyItem] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Any | Problem | Record | None:
    """Get current version of a record

     Current state of one record (head plus live). Strong ETag `"<version>.<observedAt epoch ms>"`;
    `include=observer` reads observers from the head's version when gated in.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):
        external_id (str):
        format_ (GetRecordFormat | Unset):  Default: GetRecordFormat.JSON.
        include (list[GetRecordIncludeItem] | Unset):
        fields (str | Unset):  Example: externalId,title,geometry,payload.mag.
        precision (int | Unset):  Default: 7.
        licence_policy (list[GetRecordLicencePolicyItem] | Unset):
        if_none_match (str | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | Record
    """

    return sync_detailed(
        source_id=source_id,
        external_id=external_id,
        client=client,
        format_=format_,
        include=include,
        fields=fields,
        precision=precision,
        licence_policy=licence_policy,
        if_none_match=if_none_match,
        foresignal_licence_policy=foresignal_licence_policy,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    external_id: str,
    *,
    client: AuthenticatedClient,
    format_: GetRecordFormat | Unset = GetRecordFormat.JSON,
    include: list[GetRecordIncludeItem] | Unset = UNSET,
    fields: str | Unset = UNSET,
    precision: int | Unset = 7,
    licence_policy: list[GetRecordLicencePolicyItem] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[Any | Problem | Record]:
    """Get current version of a record

     Current state of one record (head plus live). Strong ETag `"<version>.<observedAt epoch ms>"`;
    `include=observer` reads observers from the head's version when gated in.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):
        external_id (str):
        format_ (GetRecordFormat | Unset):  Default: GetRecordFormat.JSON.
        include (list[GetRecordIncludeItem] | Unset):
        fields (str | Unset):  Example: externalId,title,geometry,payload.mag.
        precision (int | Unset):  Default: 7.
        licence_policy (list[GetRecordLicencePolicyItem] | Unset):
        if_none_match (str | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | Record]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        external_id=external_id,
        format_=format_,
        include=include,
        fields=fields,
        precision=precision,
        licence_policy=licence_policy,
        if_none_match=if_none_match,
        foresignal_licence_policy=foresignal_licence_policy,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    external_id: str,
    *,
    client: AuthenticatedClient,
    format_: GetRecordFormat | Unset = GetRecordFormat.JSON,
    include: list[GetRecordIncludeItem] | Unset = UNSET,
    fields: str | Unset = UNSET,
    precision: int | Unset = 7,
    licence_policy: list[GetRecordLicencePolicyItem] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Any | Problem | Record | None:
    """Get current version of a record

     Current state of one record (head plus live). Strong ETag `"<version>.<observedAt epoch ms>"`;
    `include=observer` reads observers from the head's version when gated in.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):
        external_id (str):
        format_ (GetRecordFormat | Unset):  Default: GetRecordFormat.JSON.
        include (list[GetRecordIncludeItem] | Unset):
        fields (str | Unset):  Example: externalId,title,geometry,payload.mag.
        precision (int | Unset):  Default: 7.
        licence_policy (list[GetRecordLicencePolicyItem] | Unset):
        if_none_match (str | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | Record
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            external_id=external_id,
            client=client,
            format_=format_,
            include=include,
            fields=fields,
            precision=precision,
            licence_policy=licence_policy,
            if_none_match=if_none_match,
            foresignal_licence_policy=foresignal_licence_policy,
        )
    ).parsed
