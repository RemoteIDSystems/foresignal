// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'entity_merge_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EntityMergeInputCWProxy {
  EntityMergeInput otherId(String otherId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EntityMergeInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EntityMergeInput(...).copyWith(id: 12, name: "My name")
  /// ```
  EntityMergeInput call({String otherId});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEntityMergeInput.copyWith(...)` or call `instanceOfEntityMergeInput.copyWith.fieldName(value)` for a single field.
class _$EntityMergeInputCWProxyImpl implements _$EntityMergeInputCWProxy {
  const _$EntityMergeInputCWProxyImpl(this._value);

  final EntityMergeInput _value;

  @override
  EntityMergeInput otherId(String otherId) => call(otherId: otherId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EntityMergeInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EntityMergeInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EntityMergeInput call({Object? otherId = const $CopyWithPlaceholder()}) {
    return EntityMergeInput(
      otherId: otherId == const $CopyWithPlaceholder() || otherId == null
          ? _value.otherId
          // ignore: cast_nullable_to_non_nullable
          : otherId as String,
    );
  }
}

extension $EntityMergeInputCopyWith on EntityMergeInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEntityMergeInput.copyWith(...)` or `instanceOfEntityMergeInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EntityMergeInputCWProxy get copyWith => _$EntityMergeInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EntityMergeInput _$EntityMergeInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EntityMergeInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['otherId']);
      final val = EntityMergeInput(
        otherId: $checkedConvert('otherId', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$EntityMergeInputToJson(EntityMergeInput instance) =>
    <String, dynamic>{'otherId': instance.otherId};
