// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'llm_probe_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LlmProbeResultCWProxy {
  LlmProbeResult test(LlmTestResult test);

  LlmProbeResult models(List<LlmModelInfo> models);

  LlmProbeResult modelsError(String? modelsError);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmProbeResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmProbeResult(...).copyWith(id: 12, name: "My name")
  /// ```
  LlmProbeResult call({
    LlmTestResult test,
    List<LlmModelInfo> models,
    String? modelsError,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLlmProbeResult.copyWith(...)` or call `instanceOfLlmProbeResult.copyWith.fieldName(value)` for a single field.
class _$LlmProbeResultCWProxyImpl implements _$LlmProbeResultCWProxy {
  const _$LlmProbeResultCWProxyImpl(this._value);

  final LlmProbeResult _value;

  @override
  LlmProbeResult test(LlmTestResult test) => call(test: test);

  @override
  LlmProbeResult models(List<LlmModelInfo> models) => call(models: models);

  @override
  LlmProbeResult modelsError(String? modelsError) =>
      call(modelsError: modelsError);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmProbeResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmProbeResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LlmProbeResult call({
    Object? test = const $CopyWithPlaceholder(),
    Object? models = const $CopyWithPlaceholder(),
    Object? modelsError = const $CopyWithPlaceholder(),
  }) {
    return LlmProbeResult(
      test: test == const $CopyWithPlaceholder() || test == null
          ? _value.test
          // ignore: cast_nullable_to_non_nullable
          : test as LlmTestResult,
      models: models == const $CopyWithPlaceholder() || models == null
          ? _value.models
          // ignore: cast_nullable_to_non_nullable
          : models as List<LlmModelInfo>,
      modelsError: modelsError == const $CopyWithPlaceholder()
          ? _value.modelsError
          // ignore: cast_nullable_to_non_nullable
          : modelsError as String?,
    );
  }
}

extension $LlmProbeResultCopyWith on LlmProbeResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLlmProbeResult.copyWith(...)` or `instanceOfLlmProbeResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LlmProbeResultCWProxy get copyWith => _$LlmProbeResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LlmProbeResult _$LlmProbeResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('LlmProbeResult', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['test', 'models']);
      final val = LlmProbeResult(
        test: $checkedConvert(
          'test',
          (v) => LlmTestResult.fromJson(v as Map<String, dynamic>),
        ),
        models: $checkedConvert(
          'models',
          (v) => (v as List<dynamic>)
              .map((e) => LlmModelInfo.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        modelsError: $checkedConvert('modelsError', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$LlmProbeResultToJson(LlmProbeResult instance) =>
    <String, dynamic>{
      'test': instance.test.toJson(),
      'models': instance.models.map((e) => e.toJson()).toList(),
      'modelsError': ?instance.modelsError,
    };
