// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_llm.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoLlmCWProxy {
  SystemInfoLlm configured(bool? configured);

  SystemInfoLlm chatProvider(String? chatProvider);

  SystemInfoLlm embeddingProvider(String? embeddingProvider);

  SystemInfoLlm tokensToday(int? tokensToday);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoLlm(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoLlm(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoLlm call({
    bool? configured,
    String? chatProvider,
    String? embeddingProvider,
    int? tokensToday,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoLlm.copyWith(...)` or call `instanceOfSystemInfoLlm.copyWith.fieldName(value)` for a single field.
class _$SystemInfoLlmCWProxyImpl implements _$SystemInfoLlmCWProxy {
  const _$SystemInfoLlmCWProxyImpl(this._value);

  final SystemInfoLlm _value;

  @override
  SystemInfoLlm configured(bool? configured) => call(configured: configured);

  @override
  SystemInfoLlm chatProvider(String? chatProvider) =>
      call(chatProvider: chatProvider);

  @override
  SystemInfoLlm embeddingProvider(String? embeddingProvider) =>
      call(embeddingProvider: embeddingProvider);

  @override
  SystemInfoLlm tokensToday(int? tokensToday) => call(tokensToday: tokensToday);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoLlm(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoLlm(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoLlm call({
    Object? configured = const $CopyWithPlaceholder(),
    Object? chatProvider = const $CopyWithPlaceholder(),
    Object? embeddingProvider = const $CopyWithPlaceholder(),
    Object? tokensToday = const $CopyWithPlaceholder(),
  }) {
    return SystemInfoLlm(
      configured: configured == const $CopyWithPlaceholder()
          ? _value.configured
          // ignore: cast_nullable_to_non_nullable
          : configured as bool?,
      chatProvider: chatProvider == const $CopyWithPlaceholder()
          ? _value.chatProvider
          // ignore: cast_nullable_to_non_nullable
          : chatProvider as String?,
      embeddingProvider: embeddingProvider == const $CopyWithPlaceholder()
          ? _value.embeddingProvider
          // ignore: cast_nullable_to_non_nullable
          : embeddingProvider as String?,
      tokensToday: tokensToday == const $CopyWithPlaceholder()
          ? _value.tokensToday
          // ignore: cast_nullable_to_non_nullable
          : tokensToday as int?,
    );
  }
}

extension $SystemInfoLlmCopyWith on SystemInfoLlm {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoLlm.copyWith(...)` or `instanceOfSystemInfoLlm.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoLlmCWProxy get copyWith => _$SystemInfoLlmCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoLlm _$SystemInfoLlmFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SystemInfoLlm', json, ($checkedConvert) {
      final val = SystemInfoLlm(
        configured: $checkedConvert('configured', (v) => v as bool?),
        chatProvider: $checkedConvert('chatProvider', (v) => v as String?),
        embeddingProvider: $checkedConvert(
          'embeddingProvider',
          (v) => v as String?,
        ),
        tokensToday: $checkedConvert(
          'tokensToday',
          (v) => (v as num?)?.toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SystemInfoLlmToJson(SystemInfoLlm instance) =>
    <String, dynamic>{
      'configured': ?instance.configured,
      'chatProvider': ?instance.chatProvider,
      'embeddingProvider': ?instance.embeddingProvider,
      'tokensToday': ?instance.tokensToday,
    };
