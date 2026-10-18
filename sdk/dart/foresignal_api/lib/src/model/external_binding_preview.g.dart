// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'external_binding_preview.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExternalBindingPreviewCWProxy {
  ExternalBindingPreview evaluated(int evaluated);

  ExternalBindingPreview matched(int matched);

  ExternalBindingPreview effects(
    List<ExternalBindingPreviewEffectsInner> effects,
  );

  ExternalBindingPreview errors(List<String>? errors);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalBindingPreview(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalBindingPreview(...).copyWith(id: 12, name: "My name")
  /// ```
  ExternalBindingPreview call({
    int evaluated,
    int matched,
    List<ExternalBindingPreviewEffectsInner> effects,
    List<String>? errors,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExternalBindingPreview.copyWith(...)` or call `instanceOfExternalBindingPreview.copyWith.fieldName(value)` for a single field.
class _$ExternalBindingPreviewCWProxyImpl
    implements _$ExternalBindingPreviewCWProxy {
  const _$ExternalBindingPreviewCWProxyImpl(this._value);

  final ExternalBindingPreview _value;

  @override
  ExternalBindingPreview evaluated(int evaluated) => call(evaluated: evaluated);

  @override
  ExternalBindingPreview matched(int matched) => call(matched: matched);

  @override
  ExternalBindingPreview effects(
    List<ExternalBindingPreviewEffectsInner> effects,
  ) => call(effects: effects);

  @override
  ExternalBindingPreview errors(List<String>? errors) => call(errors: errors);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalBindingPreview(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalBindingPreview(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExternalBindingPreview call({
    Object? evaluated = const $CopyWithPlaceholder(),
    Object? matched = const $CopyWithPlaceholder(),
    Object? effects = const $CopyWithPlaceholder(),
    Object? errors = const $CopyWithPlaceholder(),
  }) {
    return ExternalBindingPreview(
      evaluated: evaluated == const $CopyWithPlaceholder() || evaluated == null
          ? _value.evaluated
          // ignore: cast_nullable_to_non_nullable
          : evaluated as int,
      matched: matched == const $CopyWithPlaceholder() || matched == null
          ? _value.matched
          // ignore: cast_nullable_to_non_nullable
          : matched as int,
      effects: effects == const $CopyWithPlaceholder() || effects == null
          ? _value.effects
          // ignore: cast_nullable_to_non_nullable
          : effects as List<ExternalBindingPreviewEffectsInner>,
      errors: errors == const $CopyWithPlaceholder()
          ? _value.errors
          // ignore: cast_nullable_to_non_nullable
          : errors as List<String>?,
    );
  }
}

extension $ExternalBindingPreviewCopyWith on ExternalBindingPreview {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExternalBindingPreview.copyWith(...)` or `instanceOfExternalBindingPreview.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExternalBindingPreviewCWProxy get copyWith =>
      _$ExternalBindingPreviewCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExternalBindingPreview _$ExternalBindingPreviewFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExternalBindingPreview', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['evaluated', 'matched', 'effects']);
  final val = ExternalBindingPreview(
    evaluated: $checkedConvert('evaluated', (v) => (v as num).toInt()),
    matched: $checkedConvert('matched', (v) => (v as num).toInt()),
    effects: $checkedConvert(
      'effects',
      (v) => (v as List<dynamic>)
          .map(
            (e) => ExternalBindingPreviewEffectsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    errors: $checkedConvert(
      'errors',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ExternalBindingPreviewToJson(
  ExternalBindingPreview instance,
) => <String, dynamic>{
  'evaluated': instance.evaluated,
  'matched': instance.matched,
  'effects': instance.effects.map((e) => e.toJson()).toList(),
  'errors': ?instance.errors,
};
