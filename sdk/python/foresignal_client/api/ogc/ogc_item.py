from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.ogc_feature import OgcFeature
from ...models.ogc_item_f import OgcItemF
from ...models.ogc_item_licence_policy_item import OgcItemLicencePolicyItem
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    collection_id: str,
    feature_id: str,
    *,
    licence_policy: list[OgcItemLicencePolicyItem] | Unset = UNSET,
    f: OgcItemF | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(foresignal_licence_policy, Unset):
        headers["Foresignal-Licence-Policy"] = foresignal_licence_policy

    params: dict[str, Any] = {}

    json_licence_policy: list[str] | Unset = UNSET
    if not isinstance(licence_policy, Unset):
        json_licence_policy = []
        for licence_policy_item_data in licence_policy:
            licence_policy_item = licence_policy_item_data.value
            json_licence_policy.append(licence_policy_item)

    params["licencePolicy"] = json_licence_policy

    json_f: str | Unset = UNSET
    if not isinstance(f, Unset):
        json_f = f.value

    params["f"] = json_f

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/ogc/collections/{collection_id}/items/{feature_id}".format(
            collection_id=quote(str(collection_id), safe=""),
            feature_id=quote(str(feature_id), safe=""),
        ),
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> OgcFeature | Problem | None:
    if response.status_code == 200:
        response_200 = OgcFeature.from_dict(response.json())

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
) -> Response[OgcFeature | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    collection_id: str,
    feature_id: str,
    *,
    client: AuthenticatedClient,
    licence_policy: list[OgcItemLicencePolicyItem] | Unset = UNSET,
    f: OgcItemF | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[OgcFeature | Problem]:
    """Get one OGC feature

     Current feature `{sourceId}:{externalId}` or history feature `{sourceId}:{externalId}:{version}`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        collection_id (str):
        feature_id (str):
        licence_policy (list[OgcItemLicencePolicyItem] | Unset):
        f (OgcItemF | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[OgcFeature | Problem]
    """

    kwargs = _get_kwargs(
        collection_id=collection_id,
        feature_id=feature_id,
        licence_policy=licence_policy,
        f=f,
        foresignal_licence_policy=foresignal_licence_policy,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    collection_id: str,
    feature_id: str,
    *,
    client: AuthenticatedClient,
    licence_policy: list[OgcItemLicencePolicyItem] | Unset = UNSET,
    f: OgcItemF | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> OgcFeature | Problem | None:
    """Get one OGC feature

     Current feature `{sourceId}:{externalId}` or history feature `{sourceId}:{externalId}:{version}`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        collection_id (str):
        feature_id (str):
        licence_policy (list[OgcItemLicencePolicyItem] | Unset):
        f (OgcItemF | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        OgcFeature | Problem
    """

    return sync_detailed(
        collection_id=collection_id,
        feature_id=feature_id,
        client=client,
        licence_policy=licence_policy,
        f=f,
        foresignal_licence_policy=foresignal_licence_policy,
    ).parsed


async def asyncio_detailed(
    collection_id: str,
    feature_id: str,
    *,
    client: AuthenticatedClient,
    licence_policy: list[OgcItemLicencePolicyItem] | Unset = UNSET,
    f: OgcItemF | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[OgcFeature | Problem]:
    """Get one OGC feature

     Current feature `{sourceId}:{externalId}` or history feature `{sourceId}:{externalId}:{version}`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        collection_id (str):
        feature_id (str):
        licence_policy (list[OgcItemLicencePolicyItem] | Unset):
        f (OgcItemF | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[OgcFeature | Problem]
    """

    kwargs = _get_kwargs(
        collection_id=collection_id,
        feature_id=feature_id,
        licence_policy=licence_policy,
        f=f,
        foresignal_licence_policy=foresignal_licence_policy,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    collection_id: str,
    feature_id: str,
    *,
    client: AuthenticatedClient,
    licence_policy: list[OgcItemLicencePolicyItem] | Unset = UNSET,
    f: OgcItemF | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> OgcFeature | Problem | None:
    """Get one OGC feature

     Current feature `{sourceId}:{externalId}` or history feature `{sourceId}:{externalId}:{version}`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        collection_id (str):
        feature_id (str):
        licence_policy (list[OgcItemLicencePolicyItem] | Unset):
        f (OgcItemF | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        OgcFeature | Problem
    """

    return (
        await asyncio_detailed(
            collection_id=collection_id,
            feature_id=feature_id,
            client=client,
            licence_policy=licence_policy,
            f=f,
            foresignal_licence_policy=foresignal_licence_policy,
        )
    ).parsed
