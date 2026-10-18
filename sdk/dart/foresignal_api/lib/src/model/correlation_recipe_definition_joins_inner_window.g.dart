// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'correlation_recipe_definition_joins_inner_window.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CorrelationRecipeDefinitionJoinsInnerWindowCWProxy {
  CorrelationRecipeDefinitionJoinsInnerWindow before(String? before);

  CorrelationRecipeDefinitionJoinsInnerWindow after(String? after);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CorrelationRecipeDefinitionJoinsInnerWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CorrelationRecipeDefinitionJoinsInnerWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  CorrelationRecipeDefinitionJoinsInnerWindow call({
    String? before,
    String? after,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCorrelationRecipeDefinitionJoinsInnerWindow.copyWith(...)` or call `instanceOfCorrelationRecipeDefinitionJoinsInnerWindow.copyWith.fieldName(value)` for a single field.
class _$CorrelationRecipeDefinitionJoinsInnerWindowCWProxyImpl
    implements _$CorrelationRecipeDefinitionJoinsInnerWindowCWProxy {
  const _$CorrelationRecipeDefinitionJoinsInnerWindowCWProxyImpl(this._value);

  final CorrelationRecipeDefinitionJoinsInnerWindow _value;

  @override
  CorrelationRecipeDefinitionJoinsInnerWindow before(String? before) =>
      call(before: before);

  @override
  CorrelationRecipeDefinitionJoinsInnerWindow after(String? after) =>
      call(after: after);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CorrelationRecipeDefinitionJoinsInnerWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CorrelationRecipeDefinitionJoinsInnerWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CorrelationRecipeDefinitionJoinsInnerWindow call({
    Object? before = const $CopyWithPlaceholder(),
    Object? after = const $CopyWithPlaceholder(),
  }) {
    return CorrelationRecipeDefinitionJoinsInnerWindow(
      before: before == const $CopyWithPlaceholder()
          ? _value.before
          // ignore: cast_nullable_to_non_nullable
          : before as String?,
      after: after == const $CopyWithPlaceholder()
          ? _value.after
          // ignore: cast_nullable_to_non_nullable
          : after as String?,
    );
  }
}

extension $CorrelationRecipeDefinitionJoinsInnerWindowCopyWith
    on CorrelationRecipeDefinitionJoinsInnerWindow {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCorrelationRecipeDefinitionJoinsInnerWindow.copyWith(...)` or `instanceOfCorrelationRecipeDefinitionJoinsInnerWindow.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CorrelationRecipeDefinitionJoinsInnerWindowCWProxy get copyWith =>
      _$CorrelationRecipeDefinitionJoinsInnerWindowCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CorrelationRecipeDefinitionJoinsInnerWindow
_$CorrelationRecipeDefinitionJoinsInnerWindowFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CorrelationRecipeDefinitionJoinsInnerWindow', json, (
  $checkedConvert,
) {
  final val = CorrelationRecipeDefinitionJoinsInnerWindow(
    before: $checkedConvert('before', (v) => v as String?),
    after: $checkedConvert('after', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$CorrelationRecipeDefinitionJoinsInnerWindowToJson(
  CorrelationRecipeDefinitionJoinsInnerWindow instance,
) => <String, dynamic>{'before': ?instance.before, 'after': ?instance.after};
