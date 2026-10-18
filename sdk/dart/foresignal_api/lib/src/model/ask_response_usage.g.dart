// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ask_response_usage.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AskResponseUsageCWProxy {
  AskResponseUsage provider(String? provider);

  AskResponseUsage model(String? model);

  AskResponseUsage inputTokens(int? inputTokens);

  AskResponseUsage outputTokens(int? outputTokens);

  AskResponseUsage toolCalls(int? toolCalls);

  AskResponseUsage durationMs(int? durationMs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskResponseUsage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskResponseUsage(...).copyWith(id: 12, name: "My name")
  /// ```
  AskResponseUsage call({
    String? provider,
    String? model,
    int? inputTokens,
    int? outputTokens,
    int? toolCalls,
    int? durationMs,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAskResponseUsage.copyWith(...)` or call `instanceOfAskResponseUsage.copyWith.fieldName(value)` for a single field.
class _$AskResponseUsageCWProxyImpl implements _$AskResponseUsageCWProxy {
  const _$AskResponseUsageCWProxyImpl(this._value);

  final AskResponseUsage _value;

  @override
  AskResponseUsage provider(String? provider) => call(provider: provider);

  @override
  AskResponseUsage model(String? model) => call(model: model);

  @override
  AskResponseUsage inputTokens(int? inputTokens) =>
      call(inputTokens: inputTokens);

  @override
  AskResponseUsage outputTokens(int? outputTokens) =>
      call(outputTokens: outputTokens);

  @override
  AskResponseUsage toolCalls(int? toolCalls) => call(toolCalls: toolCalls);

  @override
  AskResponseUsage durationMs(int? durationMs) => call(durationMs: durationMs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskResponseUsage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskResponseUsage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AskResponseUsage call({
    Object? provider = const $CopyWithPlaceholder(),
    Object? model = const $CopyWithPlaceholder(),
    Object? inputTokens = const $CopyWithPlaceholder(),
    Object? outputTokens = const $CopyWithPlaceholder(),
    Object? toolCalls = const $CopyWithPlaceholder(),
    Object? durationMs = const $CopyWithPlaceholder(),
  }) {
    return AskResponseUsage(
      provider: provider == const $CopyWithPlaceholder()
          ? _value.provider
          // ignore: cast_nullable_to_non_nullable
          : provider as String?,
      model: model == const $CopyWithPlaceholder()
          ? _value.model
          // ignore: cast_nullable_to_non_nullable
          : model as String?,
      inputTokens: inputTokens == const $CopyWithPlaceholder()
          ? _value.inputTokens
          // ignore: cast_nullable_to_non_nullable
          : inputTokens as int?,
      outputTokens: outputTokens == const $CopyWithPlaceholder()
          ? _value.outputTokens
          // ignore: cast_nullable_to_non_nullable
          : outputTokens as int?,
      toolCalls: toolCalls == const $CopyWithPlaceholder()
          ? _value.toolCalls
          // ignore: cast_nullable_to_non_nullable
          : toolCalls as int?,
      durationMs: durationMs == const $CopyWithPlaceholder()
          ? _value.durationMs
          // ignore: cast_nullable_to_non_nullable
          : durationMs as int?,
    );
  }
}

extension $AskResponseUsageCopyWith on AskResponseUsage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAskResponseUsage.copyWith(...)` or `instanceOfAskResponseUsage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AskResponseUsageCWProxy get copyWith => _$AskResponseUsageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AskResponseUsage _$AskResponseUsageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AskResponseUsage', json, ($checkedConvert) {
  final val = AskResponseUsage(
    provider: $checkedConvert('provider', (v) => v as String?),
    model: $checkedConvert('model', (v) => v as String?),
    inputTokens: $checkedConvert('inputTokens', (v) => (v as num?)?.toInt()),
    outputTokens: $checkedConvert('outputTokens', (v) => (v as num?)?.toInt()),
    toolCalls: $checkedConvert('toolCalls', (v) => (v as num?)?.toInt()),
    durationMs: $checkedConvert('durationMs', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$AskResponseUsageToJson(AskResponseUsage instance) =>
    <String, dynamic>{
      'provider': ?instance.provider,
      'model': ?instance.model,
      'inputTokens': ?instance.inputTokens,
      'outputTokens': ?instance.outputTokens,
      'toolCalls': ?instance.toolCalls,
      'durationMs': ?instance.durationMs,
    };
