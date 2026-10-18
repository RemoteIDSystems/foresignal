// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'audit_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AuditEventCWProxy {
  AuditEvent id(int id);

  AuditEvent at(DateTime at);

  AuditEvent actorKind(AuditEventActorKindEnum actorKind);

  AuditEvent tokenId(String? tokenId);

  AuditEvent tokenName(String? tokenName);

  AuditEvent principalId(String? principalId);

  AuditEvent scope(AuditEventScopeEnum? scope);

  AuditEvent method(String? method);

  AuditEvent path(String? path);

  AuditEvent operationId(String? operationId);

  AuditEvent status(int? status);

  AuditEvent targetType(String? targetType);

  AuditEvent targetId(String? targetId);

  AuditEvent remoteIp(String? remoteIp);

  AuditEvent detail(Map<String, Object>? detail);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AuditEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AuditEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  AuditEvent call({
    int id,
    DateTime at,
    AuditEventActorKindEnum actorKind,
    String? tokenId,
    String? tokenName,
    String? principalId,
    AuditEventScopeEnum? scope,
    String? method,
    String? path,
    String? operationId,
    int? status,
    String? targetType,
    String? targetId,
    String? remoteIp,
    Map<String, Object>? detail,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAuditEvent.copyWith(...)` or call `instanceOfAuditEvent.copyWith.fieldName(value)` for a single field.
class _$AuditEventCWProxyImpl implements _$AuditEventCWProxy {
  const _$AuditEventCWProxyImpl(this._value);

  final AuditEvent _value;

  @override
  AuditEvent id(int id) => call(id: id);

  @override
  AuditEvent at(DateTime at) => call(at: at);

  @override
  AuditEvent actorKind(AuditEventActorKindEnum actorKind) =>
      call(actorKind: actorKind);

  @override
  AuditEvent tokenId(String? tokenId) => call(tokenId: tokenId);

  @override
  AuditEvent tokenName(String? tokenName) => call(tokenName: tokenName);

  @override
  AuditEvent principalId(String? principalId) => call(principalId: principalId);

  @override
  AuditEvent scope(AuditEventScopeEnum? scope) => call(scope: scope);

  @override
  AuditEvent method(String? method) => call(method: method);

  @override
  AuditEvent path(String? path) => call(path: path);

  @override
  AuditEvent operationId(String? operationId) => call(operationId: operationId);

  @override
  AuditEvent status(int? status) => call(status: status);

  @override
  AuditEvent targetType(String? targetType) => call(targetType: targetType);

  @override
  AuditEvent targetId(String? targetId) => call(targetId: targetId);

  @override
  AuditEvent remoteIp(String? remoteIp) => call(remoteIp: remoteIp);

  @override
  AuditEvent detail(Map<String, Object>? detail) => call(detail: detail);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AuditEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AuditEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AuditEvent call({
    Object? id = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
    Object? actorKind = const $CopyWithPlaceholder(),
    Object? tokenId = const $CopyWithPlaceholder(),
    Object? tokenName = const $CopyWithPlaceholder(),
    Object? principalId = const $CopyWithPlaceholder(),
    Object? scope = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
    Object? path = const $CopyWithPlaceholder(),
    Object? operationId = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? targetType = const $CopyWithPlaceholder(),
    Object? targetId = const $CopyWithPlaceholder(),
    Object? remoteIp = const $CopyWithPlaceholder(),
    Object? detail = const $CopyWithPlaceholder(),
  }) {
    return AuditEvent(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as int,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      actorKind: actorKind == const $CopyWithPlaceholder() || actorKind == null
          ? _value.actorKind
          // ignore: cast_nullable_to_non_nullable
          : actorKind as AuditEventActorKindEnum,
      tokenId: tokenId == const $CopyWithPlaceholder()
          ? _value.tokenId
          // ignore: cast_nullable_to_non_nullable
          : tokenId as String?,
      tokenName: tokenName == const $CopyWithPlaceholder()
          ? _value.tokenName
          // ignore: cast_nullable_to_non_nullable
          : tokenName as String?,
      principalId: principalId == const $CopyWithPlaceholder()
          ? _value.principalId
          // ignore: cast_nullable_to_non_nullable
          : principalId as String?,
      scope: scope == const $CopyWithPlaceholder()
          ? _value.scope
          // ignore: cast_nullable_to_non_nullable
          : scope as AuditEventScopeEnum?,
      method: method == const $CopyWithPlaceholder()
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as String?,
      path: path == const $CopyWithPlaceholder()
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String?,
      operationId: operationId == const $CopyWithPlaceholder()
          ? _value.operationId
          // ignore: cast_nullable_to_non_nullable
          : operationId as String?,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as int?,
      targetType: targetType == const $CopyWithPlaceholder()
          ? _value.targetType
          // ignore: cast_nullable_to_non_nullable
          : targetType as String?,
      targetId: targetId == const $CopyWithPlaceholder()
          ? _value.targetId
          // ignore: cast_nullable_to_non_nullable
          : targetId as String?,
      remoteIp: remoteIp == const $CopyWithPlaceholder()
          ? _value.remoteIp
          // ignore: cast_nullable_to_non_nullable
          : remoteIp as String?,
      detail: detail == const $CopyWithPlaceholder()
          ? _value.detail
          // ignore: cast_nullable_to_non_nullable
          : detail as Map<String, Object>?,
    );
  }
}

extension $AuditEventCopyWith on AuditEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAuditEvent.copyWith(...)` or `instanceOfAuditEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AuditEventCWProxy get copyWith => _$AuditEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AuditEvent _$AuditEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AuditEvent', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'at', 'actorKind']);
      final val = AuditEvent(
        id: $checkedConvert('id', (v) => (v as num).toInt()),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
        actorKind: $checkedConvert(
          'actorKind',
          (v) => $enumDecode(_$AuditEventActorKindEnumEnumMap, v),
        ),
        tokenId: $checkedConvert('tokenId', (v) => v as String?),
        tokenName: $checkedConvert('tokenName', (v) => v as String?),
        principalId: $checkedConvert('principalId', (v) => v as String?),
        scope: $checkedConvert(
          'scope',
          (v) => $enumDecodeNullable(_$AuditEventScopeEnumEnumMap, v),
        ),
        method: $checkedConvert('method', (v) => v as String?),
        path: $checkedConvert('path', (v) => v as String?),
        operationId: $checkedConvert('operationId', (v) => v as String?),
        status: $checkedConvert('status', (v) => (v as num?)?.toInt()),
        targetType: $checkedConvert('targetType', (v) => v as String?),
        targetId: $checkedConvert('targetId', (v) => v as String?),
        remoteIp: $checkedConvert('remoteIp', (v) => v as String?),
        detail: $checkedConvert(
          'detail',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$AuditEventToJson(AuditEvent instance) =>
    <String, dynamic>{
      'id': instance.id,
      'at': instance.at.toIso8601String(),
      'actorKind': _$AuditEventActorKindEnumEnumMap[instance.actorKind]!,
      'tokenId': ?instance.tokenId,
      'tokenName': ?instance.tokenName,
      'principalId': ?instance.principalId,
      'scope': ?_$AuditEventScopeEnumEnumMap[instance.scope],
      'method': ?instance.method,
      'path': ?instance.path,
      'operationId': ?instance.operationId,
      'status': ?instance.status,
      'targetType': ?instance.targetType,
      'targetId': ?instance.targetId,
      'remoteIp': ?instance.remoteIp,
      'detail': ?instance.detail,
    };

const _$AuditEventActorKindEnumEnumMap = {
  AuditEventActorKindEnum.token: 'token',
  AuditEventActorKindEnum.ingestKey: 'ingest_key',
  AuditEventActorKindEnum.pairing: 'pairing',
  AuditEventActorKindEnum.system: 'system',
  AuditEventActorKindEnum.action: 'action',
  AuditEventActorKindEnum.chat: 'chat',
  AuditEventActorKindEnum.mesh: 'mesh',
};

const _$AuditEventScopeEnumEnumMap = {
  AuditEventScopeEnum.read: 'read',
  AuditEventScopeEnum.agent: 'agent',
  AuditEventScopeEnum.admin: 'admin',
};
