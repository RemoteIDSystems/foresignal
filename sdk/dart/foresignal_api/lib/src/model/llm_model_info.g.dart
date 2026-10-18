// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'llm_model_info.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LlmModelInfoCWProxy {
  LlmModelInfo id(String id);

  LlmModelInfo displayName(String? displayName);

  LlmModelInfo contextWindow(int? contextWindow);

  LlmModelInfo supportsTools(bool? supportsTools);

  LlmModelInfo supportsEmbeddings(bool? supportsEmbeddings);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmModelInfo(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmModelInfo(...).copyWith(id: 12, name: "My name")
  /// ```
  LlmModelInfo call({
    String id,
    String? displayName,
    int? contextWindow,
    bool? supportsTools,
    bool? supportsEmbeddings,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLlmModelInfo.copyWith(...)` or call `instanceOfLlmModelInfo.copyWith.fieldName(value)` for a single field.
class _$LlmModelInfoCWProxyImpl implements _$LlmModelInfoCWProxy {
  const _$LlmModelInfoCWProxyImpl(this._value);

  final LlmModelInfo _value;

  @override
  LlmModelInfo id(String id) => call(id: id);

  @override
  LlmModelInfo displayName(String? displayName) =>
      call(displayName: displayName);

  @override
  LlmModelInfo contextWindow(int? contextWindow) =>
      call(contextWindow: contextWindow);

  @override
  LlmModelInfo supportsTools(bool? supportsTools) =>
      call(supportsTools: supportsTools);

  @override
  LlmModelInfo supportsEmbeddings(bool? supportsEmbeddings) =>
      call(supportsEmbeddings: supportsEmbeddings);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmModelInfo(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmModelInfo(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LlmModelInfo call({
    Object? id = const $CopyWithPlaceholder(),
    Object? displayName = const $CopyWithPlaceholder(),
    Object? contextWindow = const $CopyWithPlaceholder(),
    Object? supportsTools = const $CopyWithPlaceholder(),
    Object? supportsEmbeddings = const $CopyWithPlaceholder(),
  }) {
    return LlmModelInfo(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      displayName: displayName == const $CopyWithPlaceholder()
          ? _value.displayName
          // ignore: cast_nullable_to_non_nullable
          : displayName as String?,
      contextWindow: contextWindow == const $CopyWithPlaceholder()
          ? _value.contextWindow
          // ignore: cast_nullable_to_non_nullable
          : contextWindow as int?,
      supportsTools: supportsTools == const $CopyWithPlaceholder()
          ? _value.supportsTools
          // ignore: cast_nullable_to_non_nullable
          : supportsTools as bool?,
      supportsEmbeddings: supportsEmbeddings == const $CopyWithPlaceholder()
          ? _value.supportsEmbeddings
          // ignore: cast_nullable_to_non_nullable
          : supportsEmbeddings as bool?,
    );
  }
}

extension $LlmModelInfoCopyWith on LlmModelInfo {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLlmModelInfo.copyWith(...)` or `instanceOfLlmModelInfo.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LlmModelInfoCWProxy get copyWith => _$LlmModelInfoCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LlmModelInfo _$LlmModelInfoFromJson(Map<String, dynamic> json) =>
    $checkedCreate('LlmModelInfo', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id']);
      final val = LlmModelInfo(
        id: $checkedConvert('id', (v) => v as String),
        displayName: $checkedConvert('displayName', (v) => v as String?),
        contextWindow: $checkedConvert(
          'contextWindow',
          (v) => (v as num?)?.toInt(),
        ),
        supportsTools: $checkedConvert('supportsTools', (v) => v as bool?),
        supportsEmbeddings: $checkedConvert(
          'supportsEmbeddings',
          (v) => v as bool?,
        ),
      );
      return val;
    });

Map<String, dynamic> _$LlmModelInfoToJson(LlmModelInfo instance) =>
    <String, dynamic>{
      'id': instance.id,
      'displayName': ?instance.displayName,
      'contextWindow': ?instance.contextWindow,
      'supportsTools': ?instance.supportsTools,
      'supportsEmbeddings': ?instance.supportsEmbeddings,
    };
