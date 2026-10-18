// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'entity.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EntityCWProxy {
  Entity id(String id);

  Entity requestedId(String? requestedId);

  Entity type(EntityTypeEnum type);

  Entity displayName(String? displayName);

  Entity attributes(Map<String, Object>? attributes);

  Entity provisional(bool provisional);

  Entity status(EntityStatusEnum status);

  Entity mergedInto(String? mergedInto);

  Entity keys(List<EntityKey>? keys);

  Entity aliases(List<EntityAlias>? aliases);

  Entity openLinkCounts(Map<String, int>? openLinkCounts);

  Entity createdAt(DateTime createdAt);

  Entity updatedAt(DateTime updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Entity(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Entity(...).copyWith(id: 12, name: "My name")
  /// ```
  Entity call({
    String id,
    String? requestedId,
    EntityTypeEnum type,
    String? displayName,
    Map<String, Object>? attributes,
    bool provisional,
    EntityStatusEnum status,
    String? mergedInto,
    List<EntityKey>? keys,
    List<EntityAlias>? aliases,
    Map<String, int>? openLinkCounts,
    DateTime createdAt,
    DateTime updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEntity.copyWith(...)` or call `instanceOfEntity.copyWith.fieldName(value)` for a single field.
class _$EntityCWProxyImpl implements _$EntityCWProxy {
  const _$EntityCWProxyImpl(this._value);

  final Entity _value;

  @override
  Entity id(String id) => call(id: id);

  @override
  Entity requestedId(String? requestedId) => call(requestedId: requestedId);

  @override
  Entity type(EntityTypeEnum type) => call(type: type);

  @override
  Entity displayName(String? displayName) => call(displayName: displayName);

  @override
  Entity attributes(Map<String, Object>? attributes) =>
      call(attributes: attributes);

  @override
  Entity provisional(bool provisional) => call(provisional: provisional);

  @override
  Entity status(EntityStatusEnum status) => call(status: status);

  @override
  Entity mergedInto(String? mergedInto) => call(mergedInto: mergedInto);

  @override
  Entity keys(List<EntityKey>? keys) => call(keys: keys);

  @override
  Entity aliases(List<EntityAlias>? aliases) => call(aliases: aliases);

  @override
  Entity openLinkCounts(Map<String, int>? openLinkCounts) =>
      call(openLinkCounts: openLinkCounts);

  @override
  Entity createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Entity updatedAt(DateTime updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Entity(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Entity(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Entity call({
    Object? id = const $CopyWithPlaceholder(),
    Object? requestedId = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? displayName = const $CopyWithPlaceholder(),
    Object? attributes = const $CopyWithPlaceholder(),
    Object? provisional = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? mergedInto = const $CopyWithPlaceholder(),
    Object? keys = const $CopyWithPlaceholder(),
    Object? aliases = const $CopyWithPlaceholder(),
    Object? openLinkCounts = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return Entity(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      requestedId: requestedId == const $CopyWithPlaceholder()
          ? _value.requestedId
          // ignore: cast_nullable_to_non_nullable
          : requestedId as String?,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as EntityTypeEnum,
      displayName: displayName == const $CopyWithPlaceholder()
          ? _value.displayName
          // ignore: cast_nullable_to_non_nullable
          : displayName as String?,
      attributes: attributes == const $CopyWithPlaceholder()
          ? _value.attributes
          // ignore: cast_nullable_to_non_nullable
          : attributes as Map<String, Object>?,
      provisional:
          provisional == const $CopyWithPlaceholder() || provisional == null
          ? _value.provisional
          // ignore: cast_nullable_to_non_nullable
          : provisional as bool,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as EntityStatusEnum,
      mergedInto: mergedInto == const $CopyWithPlaceholder()
          ? _value.mergedInto
          // ignore: cast_nullable_to_non_nullable
          : mergedInto as String?,
      keys: keys == const $CopyWithPlaceholder()
          ? _value.keys
          // ignore: cast_nullable_to_non_nullable
          : keys as List<EntityKey>?,
      aliases: aliases == const $CopyWithPlaceholder()
          ? _value.aliases
          // ignore: cast_nullable_to_non_nullable
          : aliases as List<EntityAlias>?,
      openLinkCounts: openLinkCounts == const $CopyWithPlaceholder()
          ? _value.openLinkCounts
          // ignore: cast_nullable_to_non_nullable
          : openLinkCounts as Map<String, int>?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder() || updatedAt == null
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime,
    );
  }
}

extension $EntityCopyWith on Entity {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEntity.copyWith(...)` or `instanceOfEntity.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EntityCWProxy get copyWith => _$EntityCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Entity _$EntityFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Entity', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'type',
      'provisional',
      'status',
      'createdAt',
      'updatedAt',
    ],
  );
  final val = Entity(
    id: $checkedConvert('id', (v) => v as String),
    requestedId: $checkedConvert('requestedId', (v) => v as String?),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$EntityTypeEnumEnumMap, v),
    ),
    displayName: $checkedConvert('displayName', (v) => v as String?),
    attributes: $checkedConvert(
      'attributes',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    provisional: $checkedConvert('provisional', (v) => v as bool),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$EntityStatusEnumEnumMap, v),
    ),
    mergedInto: $checkedConvert('mergedInto', (v) => v as String?),
    keys: $checkedConvert(
      'keys',
      (v) => (v as List<dynamic>?)
          ?.map((e) => EntityKey.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    aliases: $checkedConvert(
      'aliases',
      (v) => (v as List<dynamic>?)
          ?.map((e) => EntityAlias.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    openLinkCounts: $checkedConvert(
      'openLinkCounts',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(k, (e as num).toInt()),
      ),
    ),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    updatedAt: $checkedConvert('updatedAt', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$EntityToJson(Entity instance) => <String, dynamic>{
  'id': instance.id,
  'requestedId': ?instance.requestedId,
  'type': _$EntityTypeEnumEnumMap[instance.type]!,
  'displayName': ?instance.displayName,
  'attributes': ?instance.attributes,
  'provisional': instance.provisional,
  'status': _$EntityStatusEnumEnumMap[instance.status]!,
  'mergedInto': ?instance.mergedInto,
  'keys': ?instance.keys?.map((e) => e.toJson()).toList(),
  'aliases': ?instance.aliases?.map((e) => e.toJson()).toList(),
  'openLinkCounts': ?instance.openLinkCounts,
  'createdAt': instance.createdAt.toIso8601String(),
  'updatedAt': instance.updatedAt.toIso8601String(),
};

const _$EntityTypeEnumEnumMap = {
  EntityTypeEnum.aircraft: 'aircraft',
  EntityTypeEnum.vessel: 'vessel',
  EntityTypeEnum.satellite: 'satellite',
  EntityTypeEnum.station: 'station',
};

const _$EntityStatusEnumEnumMap = {
  EntityStatusEnum.ACTIVE: 'ACTIVE',
  EntityStatusEnum.MERGED: 'MERGED',
};
