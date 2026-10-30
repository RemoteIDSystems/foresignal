// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'news_article_translation_provenance.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NewsArticleTranslationProvenanceCWProxy {
  NewsArticleTranslationProvenance engine(String engine);

  NewsArticleTranslationProvenance model(String? model);

  NewsArticleTranslationProvenance at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NewsArticleTranslationProvenance(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NewsArticleTranslationProvenance(...).copyWith(id: 12, name: "My name")
  /// ```
  NewsArticleTranslationProvenance call({
    String engine,
    String? model,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNewsArticleTranslationProvenance.copyWith(...)` or call `instanceOfNewsArticleTranslationProvenance.copyWith.fieldName(value)` for a single field.
class _$NewsArticleTranslationProvenanceCWProxyImpl
    implements _$NewsArticleTranslationProvenanceCWProxy {
  const _$NewsArticleTranslationProvenanceCWProxyImpl(this._value);

  final NewsArticleTranslationProvenance _value;

  @override
  NewsArticleTranslationProvenance engine(String engine) =>
      call(engine: engine);

  @override
  NewsArticleTranslationProvenance model(String? model) => call(model: model);

  @override
  NewsArticleTranslationProvenance at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NewsArticleTranslationProvenance(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NewsArticleTranslationProvenance(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NewsArticleTranslationProvenance call({
    Object? engine = const $CopyWithPlaceholder(),
    Object? model = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return NewsArticleTranslationProvenance(
      engine: engine == const $CopyWithPlaceholder() || engine == null
          ? _value.engine
          // ignore: cast_nullable_to_non_nullable
          : engine as String,
      model: model == const $CopyWithPlaceholder()
          ? _value.model
          // ignore: cast_nullable_to_non_nullable
          : model as String?,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $NewsArticleTranslationProvenanceCopyWith
    on NewsArticleTranslationProvenance {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNewsArticleTranslationProvenance.copyWith(...)` or `instanceOfNewsArticleTranslationProvenance.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NewsArticleTranslationProvenanceCWProxy get copyWith =>
      _$NewsArticleTranslationProvenanceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NewsArticleTranslationProvenance _$NewsArticleTranslationProvenanceFromJson(
  Map<String, dynamic> json,
) =>
    $checkedCreate('NewsArticleTranslationProvenance', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['engine', 'at']);
      final val = NewsArticleTranslationProvenance(
        engine: $checkedConvert('engine', (v) => v as String),
        model: $checkedConvert('model', (v) => v as String?),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
      );
      return val;
    });

Map<String, dynamic> _$NewsArticleTranslationProvenanceToJson(
  NewsArticleTranslationProvenance instance,
) => <String, dynamic>{
  'engine': instance.engine,
  'model': ?instance.model,
  'at': instance.at.toIso8601String(),
};
