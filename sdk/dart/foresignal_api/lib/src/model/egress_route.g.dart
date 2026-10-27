// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'egress_route.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EgressRouteCWProxy {
  EgressRoute id(String id);

  EgressRoute name(String name);

  EgressRoute type(String type);

  EgressRoute enabled(bool enabled);

  EgressRoute url(String? url);

  EgressRoute boundInterface(String? boundInterface);

  EgressRoute exitIp(String? exitIp);

  EgressRoute health(EgressHealth? health);

  EgressRoute secretsSet(List<String>? secretsSet);

  EgressRoute createdAt(DateTime? createdAt);

  EgressRoute updatedAt(DateTime? updatedAt);

  EgressRoute available(bool? available);

  EgressRoute unavailableReason(String? unavailableReason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EgressRoute(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EgressRoute(...).copyWith(id: 12, name: "My name")
  /// ```
  EgressRoute call({
    String id,
    String name,
    String type,
    bool enabled,
    String? url,
    String? boundInterface,
    String? exitIp,
    EgressHealth? health,
    List<String>? secretsSet,
    DateTime? createdAt,
    DateTime? updatedAt,
    bool? available,
    String? unavailableReason,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEgressRoute.copyWith(...)` or call `instanceOfEgressRoute.copyWith.fieldName(value)` for a single field.
class _$EgressRouteCWProxyImpl implements _$EgressRouteCWProxy {
  const _$EgressRouteCWProxyImpl(this._value);

  final EgressRoute _value;

  @override
  EgressRoute id(String id) => call(id: id);

  @override
  EgressRoute name(String name) => call(name: name);

  @override
  EgressRoute type(String type) => call(type: type);

  @override
  EgressRoute enabled(bool enabled) => call(enabled: enabled);

  @override
  EgressRoute url(String? url) => call(url: url);

  @override
  EgressRoute boundInterface(String? boundInterface) =>
      call(boundInterface: boundInterface);

  @override
  EgressRoute exitIp(String? exitIp) => call(exitIp: exitIp);

  @override
  EgressRoute health(EgressHealth? health) => call(health: health);

  @override
  EgressRoute secretsSet(List<String>? secretsSet) =>
      call(secretsSet: secretsSet);

  @override
  EgressRoute createdAt(DateTime? createdAt) => call(createdAt: createdAt);

  @override
  EgressRoute updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  @override
  EgressRoute available(bool? available) => call(available: available);

  @override
  EgressRoute unavailableReason(String? unavailableReason) =>
      call(unavailableReason: unavailableReason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EgressRoute(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EgressRoute(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EgressRoute call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? boundInterface = const $CopyWithPlaceholder(),
    Object? exitIp = const $CopyWithPlaceholder(),
    Object? health = const $CopyWithPlaceholder(),
    Object? secretsSet = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? available = const $CopyWithPlaceholder(),
    Object? unavailableReason = const $CopyWithPlaceholder(),
  }) {
    return EgressRoute(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as String,
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
      boundInterface: boundInterface == const $CopyWithPlaceholder()
          ? _value.boundInterface
          // ignore: cast_nullable_to_non_nullable
          : boundInterface as String?,
      exitIp: exitIp == const $CopyWithPlaceholder()
          ? _value.exitIp
          // ignore: cast_nullable_to_non_nullable
          : exitIp as String?,
      health: health == const $CopyWithPlaceholder()
          ? _value.health
          // ignore: cast_nullable_to_non_nullable
          : health as EgressHealth?,
      secretsSet: secretsSet == const $CopyWithPlaceholder()
          ? _value.secretsSet
          // ignore: cast_nullable_to_non_nullable
          : secretsSet as List<String>?,
      createdAt: createdAt == const $CopyWithPlaceholder()
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
      available: available == const $CopyWithPlaceholder()
          ? _value.available
          // ignore: cast_nullable_to_non_nullable
          : available as bool?,
      unavailableReason: unavailableReason == const $CopyWithPlaceholder()
          ? _value.unavailableReason
          // ignore: cast_nullable_to_non_nullable
          : unavailableReason as String?,
    );
  }
}

extension $EgressRouteCopyWith on EgressRoute {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEgressRoute.copyWith(...)` or `instanceOfEgressRoute.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EgressRouteCWProxy get copyWith => _$EgressRouteCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EgressRoute _$EgressRouteFromJson(Map<String, dynamic> json) => $checkedCreate(
  'EgressRoute',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['id', 'name', 'type', 'enabled']);
    final val = EgressRoute(
      id: $checkedConvert('id', (v) => v as String),
      name: $checkedConvert('name', (v) => v as String),
      type: $checkedConvert('type', (v) => v as String),
      enabled: $checkedConvert('enabled', (v) => v as bool? ?? true),
      url: $checkedConvert('url', (v) => v as String?),
      boundInterface: $checkedConvert('boundInterface', (v) => v as String?),
      exitIp: $checkedConvert('exitIp', (v) => v as String?),
      health: $checkedConvert(
        'health',
        (v) =>
            v == null ? null : EgressHealth.fromJson(v as Map<String, dynamic>),
      ),
      secretsSet: $checkedConvert(
        'secretsSet',
        (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
      ),
      createdAt: $checkedConvert(
        'createdAt',
        (v) => v == null ? null : DateTime.parse(v as String),
      ),
      updatedAt: $checkedConvert(
        'updatedAt',
        (v) => v == null ? null : DateTime.parse(v as String),
      ),
      available: $checkedConvert('available', (v) => v as bool?),
      unavailableReason: $checkedConvert(
        'unavailableReason',
        (v) => v as String?,
      ),
    );
    return val;
  },
);

Map<String, dynamic> _$EgressRouteToJson(EgressRoute instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'type': instance.type,
      'enabled': instance.enabled,
      'url': ?instance.url,
      'boundInterface': ?instance.boundInterface,
      'exitIp': ?instance.exitIp,
      'health': ?instance.health?.toJson(),
      'secretsSet': ?instance.secretsSet,
      'createdAt': ?instance.createdAt?.toIso8601String(),
      'updatedAt': ?instance.updatedAt?.toIso8601String(),
      'available': ?instance.available,
      'unavailableReason': ?instance.unavailableReason,
    };
