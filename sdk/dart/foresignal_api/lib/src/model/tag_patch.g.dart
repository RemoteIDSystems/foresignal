// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tag_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TagPatchCWProxy {
  TagPatch name(String? name);

  TagPatch color(String? color);

  TagPatch description(String? description);

  TagPatch visibility(Visibility? visibility);

  TagPatch tlp(Tlp? tlp);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TagPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TagPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  TagPatch call({
    String? name,
    String? color,
    String? description,
    Visibility? visibility,
    Tlp? tlp,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTagPatch.copyWith(...)` or call `instanceOfTagPatch.copyWith.fieldName(value)` for a single field.
class _$TagPatchCWProxyImpl implements _$TagPatchCWProxy {
  const _$TagPatchCWProxyImpl(this._value);

  final TagPatch _value;

  @override
  TagPatch name(String? name) => call(name: name);

  @override
  TagPatch color(String? color) => call(color: color);

  @override
  TagPatch description(String? description) => call(description: description);

  @override
  TagPatch visibility(Visibility? visibility) => call(visibility: visibility);

  @override
  TagPatch tlp(Tlp? tlp) => call(tlp: tlp);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TagPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TagPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TagPatch call({
    Object? name = const $CopyWithPlaceholder(),
    Object? color = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? visibility = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
  }) {
    return TagPatch(
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      color: color == const $CopyWithPlaceholder()
          ? _value.color
          // ignore: cast_nullable_to_non_nullable
          : color as String?,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      visibility: visibility == const $CopyWithPlaceholder()
          ? _value.visibility
          // ignore: cast_nullable_to_non_nullable
          : visibility as Visibility?,
      tlp: tlp == const $CopyWithPlaceholder()
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp?,
    );
  }
}

extension $TagPatchCopyWith on TagPatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTagPatch.copyWith(...)` or `instanceOfTagPatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TagPatchCWProxy get copyWith => _$TagPatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TagPatch _$TagPatchFromJson(Map<String, dynamic> json) => $checkedCreate(
  'TagPatch',
  json,
  ($checkedConvert) {
    final val = TagPatch(
      name: $checkedConvert('name', (v) => v as String?),
      color: $checkedConvert('color', (v) => v as String?),
      description: $checkedConvert('description', (v) => v as String?),
      visibility: $checkedConvert(
        'visibility',
        (v) => $enumDecodeNullable(_$VisibilityEnumMap, v),
      ),
      tlp: $checkedConvert('tlp', (v) => $enumDecodeNullable(_$TlpEnumMap, v)),
    );
    return val;
  },
);

Map<String, dynamic> _$TagPatchToJson(TagPatch instance) => <String, dynamic>{
  'name': ?instance.name,
  'color': ?instance.color,
  'description': ?instance.description,
  'visibility': ?_$VisibilityEnumMap[instance.visibility],
  'tlp': ?_$TlpEnumMap[instance.tlp],
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
