// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tag.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TagCWProxy {
  Tag id(String id);

  Tag name(String name);

  Tag namespace(TagNamespaceEnum namespace);

  Tag color(String? color);

  Tag description(String? description);

  Tag owner(PrincipalSummary owner);

  Tag visibility(Visibility visibility);

  Tag tlp(Tlp? tlp);

  Tag objectCount(int? objectCount);

  Tag createdAt(DateTime createdAt);

  Tag updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Tag(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Tag(...).copyWith(id: 12, name: "My name")
  /// ```
  Tag call({
    String id,
    String name,
    TagNamespaceEnum namespace,
    String? color,
    String? description,
    PrincipalSummary owner,
    Visibility visibility,
    Tlp? tlp,
    int? objectCount,
    DateTime createdAt,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTag.copyWith(...)` or call `instanceOfTag.copyWith.fieldName(value)` for a single field.
class _$TagCWProxyImpl implements _$TagCWProxy {
  const _$TagCWProxyImpl(this._value);

  final Tag _value;

  @override
  Tag id(String id) => call(id: id);

  @override
  Tag name(String name) => call(name: name);

  @override
  Tag namespace(TagNamespaceEnum namespace) => call(namespace: namespace);

  @override
  Tag color(String? color) => call(color: color);

  @override
  Tag description(String? description) => call(description: description);

  @override
  Tag owner(PrincipalSummary owner) => call(owner: owner);

  @override
  Tag visibility(Visibility visibility) => call(visibility: visibility);

  @override
  Tag tlp(Tlp? tlp) => call(tlp: tlp);

  @override
  Tag objectCount(int? objectCount) => call(objectCount: objectCount);

  @override
  Tag createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Tag updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Tag(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Tag(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Tag call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? namespace = const $CopyWithPlaceholder(),
    Object? color = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? owner = const $CopyWithPlaceholder(),
    Object? visibility = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? objectCount = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return Tag(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      namespace: namespace == const $CopyWithPlaceholder() || namespace == null
          ? _value.namespace
          // ignore: cast_nullable_to_non_nullable
          : namespace as TagNamespaceEnum,
      color: color == const $CopyWithPlaceholder()
          ? _value.color
          // ignore: cast_nullable_to_non_nullable
          : color as String?,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      owner: owner == const $CopyWithPlaceholder() || owner == null
          ? _value.owner
          // ignore: cast_nullable_to_non_nullable
          : owner as PrincipalSummary,
      visibility:
          visibility == const $CopyWithPlaceholder() || visibility == null
          ? _value.visibility
          // ignore: cast_nullable_to_non_nullable
          : visibility as Visibility,
      tlp: tlp == const $CopyWithPlaceholder()
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp?,
      objectCount: objectCount == const $CopyWithPlaceholder()
          ? _value.objectCount
          // ignore: cast_nullable_to_non_nullable
          : objectCount as int?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $TagCopyWith on Tag {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTag.copyWith(...)` or `instanceOfTag.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TagCWProxy get copyWith => _$TagCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Tag _$TagFromJson(Map<String, dynamic> json) => $checkedCreate('Tag', json, (
  $checkedConvert,
) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'name',
      'namespace',
      'owner',
      'visibility',
      'createdAt',
    ],
  );
  final val = Tag(
    id: $checkedConvert('id', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    namespace: $checkedConvert(
      'namespace',
      (v) => $enumDecode(_$TagNamespaceEnumEnumMap, v),
    ),
    color: $checkedConvert('color', (v) => v as String?),
    description: $checkedConvert('description', (v) => v as String?),
    owner: $checkedConvert(
      'owner',
      (v) => PrincipalSummary.fromJson(v as Map<String, dynamic>),
    ),
    visibility: $checkedConvert(
      'visibility',
      (v) => $enumDecode(_$VisibilityEnumMap, v),
    ),
    tlp: $checkedConvert('tlp', (v) => $enumDecodeNullable(_$TlpEnumMap, v)),
    objectCount: $checkedConvert('objectCount', (v) => (v as num?)?.toInt()),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$TagToJson(Tag instance) => <String, dynamic>{
  'id': instance.id,
  'name': instance.name,
  'namespace': _$TagNamespaceEnumEnumMap[instance.namespace]!,
  'color': ?instance.color,
  'description': ?instance.description,
  'owner': instance.owner.toJson(),
  'visibility': _$VisibilityEnumMap[instance.visibility]!,
  'tlp': ?_$TlpEnumMap[instance.tlp],
  'objectCount': ?instance.objectCount,
  'createdAt': instance.createdAt.toIso8601String(),
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
};

const _$TagNamespaceEnumEnumMap = {
  TagNamespaceEnum.personal: 'personal',
  TagNamespaceEnum.shared: 'shared',
};

const _$VisibilityEnumMap = {
  Visibility.private: 'private',
  Visibility.shared: 'shared',
  Visibility.instance: 'instance',
};

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};
