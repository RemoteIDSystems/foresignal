// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'recipe_test_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RecipeTestRequestCWProxy {
  RecipeTestRequest fromSeq(int fromSeq);

  RecipeTestRequest toSeq(int toSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecipeTestRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecipeTestRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  RecipeTestRequest call({int fromSeq, int toSeq});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRecipeTestRequest.copyWith(...)` or call `instanceOfRecipeTestRequest.copyWith.fieldName(value)` for a single field.
class _$RecipeTestRequestCWProxyImpl implements _$RecipeTestRequestCWProxy {
  const _$RecipeTestRequestCWProxyImpl(this._value);

  final RecipeTestRequest _value;

  @override
  RecipeTestRequest fromSeq(int fromSeq) => call(fromSeq: fromSeq);

  @override
  RecipeTestRequest toSeq(int toSeq) => call(toSeq: toSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecipeTestRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecipeTestRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RecipeTestRequest call({
    Object? fromSeq = const $CopyWithPlaceholder(),
    Object? toSeq = const $CopyWithPlaceholder(),
  }) {
    return RecipeTestRequest(
      fromSeq: fromSeq == const $CopyWithPlaceholder() || fromSeq == null
          ? _value.fromSeq
          // ignore: cast_nullable_to_non_nullable
          : fromSeq as int,
      toSeq: toSeq == const $CopyWithPlaceholder() || toSeq == null
          ? _value.toSeq
          // ignore: cast_nullable_to_non_nullable
          : toSeq as int,
    );
  }
}

extension $RecipeTestRequestCopyWith on RecipeTestRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRecipeTestRequest.copyWith(...)` or `instanceOfRecipeTestRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RecipeTestRequestCWProxy get copyWith =>
      _$RecipeTestRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RecipeTestRequest _$RecipeTestRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RecipeTestRequest', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['fromSeq', 'toSeq']);
      final val = RecipeTestRequest(
        fromSeq: $checkedConvert('fromSeq', (v) => (v as num).toInt()),
        toSeq: $checkedConvert('toSeq', (v) => (v as num).toInt()),
      );
      return val;
    });

Map<String, dynamic> _$RecipeTestRequestToJson(RecipeTestRequest instance) =>
    <String, dynamic>{'fromSeq': instance.fromSeq, 'toSeq': instance.toSeq};
