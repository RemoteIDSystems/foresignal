// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tag_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TagInputCWProxy {
  TagInput name(String name);

  TagInput namespace(TagInputNamespaceEnum? namespace);

  TagInput color(String? color);

  TagInput description(String? description);

  TagInput visibility(Visibility? visibility);

  TagInput tlp(Tlp? tlp);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TagInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TagInput(...).copyWith(id: 12, name: "My name")
  /// ```
  TagInput call({
    String name,
    TagInputNamespaceEnum? namespace,
    String? color,
    String? description,
    Visibility? visibility,
    Tlp? tlp,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTagInput.copyWith(...)` or call `instanceOfTagInput.copyWith.fieldName(value)` for a single field.
class _$TagInputCWProxyImpl implements _$TagInputCWProxy {
  const _$TagInputCWProxyImpl(this._value);

  final TagInput _value;

  @override
  TagInput name(String name) => call(name: name);

  @override
  TagInput namespace(TagInputNamespaceEnum? namespace) =>
      call(namespace: namespace);

  @override
  TagInput color(String? color) => call(color: color);

  @override
  TagInput description(String? description) => call(description: description);

  @override
  TagInput visibility(Visibility? visibility) => call(visibility: visibility);

  @override
  TagInput tlp(Tlp? tlp) => call(tlp: tlp);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TagInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TagInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TagInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? namespace = const $CopyWithPlaceholder(),
    Object? color = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? visibility = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
  }) {
    return TagInput(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      namespace: namespace == const $CopyWithPlaceholder()
          ? _value.namespace
          // ignore: cast_nullable_to_non_nullable
          : namespace as TagInputNamespaceEnum?,
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

extension $TagInputCopyWith on TagInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTagInput.copyWith(...)` or `instanceOfTagInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TagInputCWProxy get copyWith => _$TagInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TagInput _$TagInputFromJson(Map<String, dynamic> json) => $checkedCreate(
  'TagInput',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['name']);
    final val = TagInput(
      name: $checkedConvert('name', (v) => v as String),
      namespace: $checkedConvert(
        'namespace',
        (v) => $enumDecodeNullable(_$TagInputNamespaceEnumEnumMap, v),
      ),
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

Map<String, dynamic> _$TagInputToJson(TagInput instance) => <String, dynamic>{
  'name': instance.name,
  'namespace': ?_$TagInputNamespaceEnumEnumMap[instance.namespace],
  'color': ?instance.color,
  'description': ?instance.description,
  'visibility': ?_$VisibilityEnumMap[instance.visibility],
  'tlp': ?_$TlpEnumMap[instance.tlp],
};

const _$TagInputNamespaceEnumEnumMap = {
  TagInputNamespaceEnum.personal: 'personal',
  TagInputNamespaceEnum.shared: 'shared',
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
