// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'group_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GroupPatchCWProxy {
  GroupPatch name(String? name);

  GroupPatch description(String? description);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GroupPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GroupPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  GroupPatch call({String? name, String? description});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGroupPatch.copyWith(...)` or call `instanceOfGroupPatch.copyWith.fieldName(value)` for a single field.
class _$GroupPatchCWProxyImpl implements _$GroupPatchCWProxy {
  const _$GroupPatchCWProxyImpl(this._value);

  final GroupPatch _value;

  @override
  GroupPatch name(String? name) => call(name: name);

  @override
  GroupPatch description(String? description) => call(description: description);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GroupPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GroupPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GroupPatch call({
    Object? name = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
  }) {
    return GroupPatch(
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
    );
  }
}

extension $GroupPatchCopyWith on GroupPatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGroupPatch.copyWith(...)` or `instanceOfGroupPatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GroupPatchCWProxy get copyWith => _$GroupPatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GroupPatch _$GroupPatchFromJson(Map<String, dynamic> json) =>
    $checkedCreate('GroupPatch', json, ($checkedConvert) {
      final val = GroupPatch(
        name: $checkedConvert('name', (v) => v as String?),
        description: $checkedConvert('description', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$GroupPatchToJson(GroupPatch instance) =>
    <String, dynamic>{
      'name': ?instance.name,
      'description': ?instance.description,
    };
