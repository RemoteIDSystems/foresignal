// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'correlation_recipe_definition_activate_at.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CorrelationRecipeDefinitionActivateAtCWProxy {
  CorrelationRecipeDefinitionActivateAt members(int? members);

  CorrelationRecipeDefinitionActivateAt confidence(num? confidence);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CorrelationRecipeDefinitionActivateAt(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CorrelationRecipeDefinitionActivateAt(...).copyWith(id: 12, name: "My name")
  /// ```
  CorrelationRecipeDefinitionActivateAt call({int? members, num? confidence});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCorrelationRecipeDefinitionActivateAt.copyWith(...)` or call `instanceOfCorrelationRecipeDefinitionActivateAt.copyWith.fieldName(value)` for a single field.
class _$CorrelationRecipeDefinitionActivateAtCWProxyImpl
    implements _$CorrelationRecipeDefinitionActivateAtCWProxy {
  const _$CorrelationRecipeDefinitionActivateAtCWProxyImpl(this._value);

  final CorrelationRecipeDefinitionActivateAt _value;

  @override
  CorrelationRecipeDefinitionActivateAt members(int? members) =>
      call(members: members);

  @override
  CorrelationRecipeDefinitionActivateAt confidence(num? confidence) =>
      call(confidence: confidence);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CorrelationRecipeDefinitionActivateAt(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CorrelationRecipeDefinitionActivateAt(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CorrelationRecipeDefinitionActivateAt call({
    Object? members = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
  }) {
    return CorrelationRecipeDefinitionActivateAt(
      members: members == const $CopyWithPlaceholder()
          ? _value.members
          // ignore: cast_nullable_to_non_nullable
          : members as int?,
      confidence: confidence == const $CopyWithPlaceholder()
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as num?,
    );
  }
}

extension $CorrelationRecipeDefinitionActivateAtCopyWith
    on CorrelationRecipeDefinitionActivateAt {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCorrelationRecipeDefinitionActivateAt.copyWith(...)` or `instanceOfCorrelationRecipeDefinitionActivateAt.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CorrelationRecipeDefinitionActivateAtCWProxy get copyWith =>
      _$CorrelationRecipeDefinitionActivateAtCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CorrelationRecipeDefinitionActivateAt
_$CorrelationRecipeDefinitionActivateAtFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CorrelationRecipeDefinitionActivateAt', json, (
      $checkedConvert,
    ) {
      final val = CorrelationRecipeDefinitionActivateAt(
        members: $checkedConvert('members', (v) => (v as num?)?.toInt()),
        confidence: $checkedConvert('confidence', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$CorrelationRecipeDefinitionActivateAtToJson(
  CorrelationRecipeDefinitionActivateAt instance,
) => <String, dynamic>{
  'members': ?instance.members,
  'confidence': ?instance.confidence,
};
