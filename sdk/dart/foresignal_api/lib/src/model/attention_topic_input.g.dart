// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'attention_topic_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AttentionTopicInputCWProxy {
  AttentionTopicInput id(String id);

  AttentionTopicInput name(String name);

  AttentionTopicInput description(String? description);

  AttentionTopicInput gdeltQuery(String? gdeltQuery);

  AttentionTopicInput wikiArticles(
    List<AttentionTopicInputWikiArticlesInner>? wikiArticles,
  );

  AttentionTopicInput keywords(List<String>? keywords);

  AttentionTopicInput regionCodes(List<String>? regionCodes);

  AttentionTopicInput themes(List<String>? themes);

  AttentionTopicInput enabled(bool? enabled);

  AttentionTopicInput social(AttentionTopicSocial? social);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AttentionTopicInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AttentionTopicInput(...).copyWith(id: 12, name: "My name")
  /// ```
  AttentionTopicInput call({
    String id,
    String name,
    String? description,
    String? gdeltQuery,
    List<AttentionTopicInputWikiArticlesInner>? wikiArticles,
    List<String>? keywords,
    List<String>? regionCodes,
    List<String>? themes,
    bool? enabled,
    AttentionTopicSocial? social,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAttentionTopicInput.copyWith(...)` or call `instanceOfAttentionTopicInput.copyWith.fieldName(value)` for a single field.
class _$AttentionTopicInputCWProxyImpl implements _$AttentionTopicInputCWProxy {
  const _$AttentionTopicInputCWProxyImpl(this._value);

  final AttentionTopicInput _value;

  @override
  AttentionTopicInput id(String id) => call(id: id);

  @override
  AttentionTopicInput name(String name) => call(name: name);

  @override
  AttentionTopicInput description(String? description) =>
      call(description: description);

  @override
  AttentionTopicInput gdeltQuery(String? gdeltQuery) =>
      call(gdeltQuery: gdeltQuery);

  @override
  AttentionTopicInput wikiArticles(
    List<AttentionTopicInputWikiArticlesInner>? wikiArticles,
  ) => call(wikiArticles: wikiArticles);

  @override
  AttentionTopicInput keywords(List<String>? keywords) =>
      call(keywords: keywords);

  @override
  AttentionTopicInput regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  AttentionTopicInput themes(List<String>? themes) => call(themes: themes);

  @override
  AttentionTopicInput enabled(bool? enabled) => call(enabled: enabled);

  @override
  AttentionTopicInput social(AttentionTopicSocial? social) =>
      call(social: social);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AttentionTopicInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AttentionTopicInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AttentionTopicInput call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? gdeltQuery = const $CopyWithPlaceholder(),
    Object? wikiArticles = const $CopyWithPlaceholder(),
    Object? keywords = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? themes = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? social = const $CopyWithPlaceholder(),
  }) {
    return AttentionTopicInput(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      gdeltQuery: gdeltQuery == const $CopyWithPlaceholder()
          ? _value.gdeltQuery
          // ignore: cast_nullable_to_non_nullable
          : gdeltQuery as String?,
      wikiArticles: wikiArticles == const $CopyWithPlaceholder()
          ? _value.wikiArticles
          // ignore: cast_nullable_to_non_nullable
          : wikiArticles as List<AttentionTopicInputWikiArticlesInner>?,
      keywords: keywords == const $CopyWithPlaceholder()
          ? _value.keywords
          // ignore: cast_nullable_to_non_nullable
          : keywords as List<String>?,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
      themes: themes == const $CopyWithPlaceholder()
          ? _value.themes
          // ignore: cast_nullable_to_non_nullable
          : themes as List<String>?,
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      social: social == const $CopyWithPlaceholder()
          ? _value.social
          // ignore: cast_nullable_to_non_nullable
          : social as AttentionTopicSocial?,
    );
  }
}

extension $AttentionTopicInputCopyWith on AttentionTopicInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAttentionTopicInput.copyWith(...)` or `instanceOfAttentionTopicInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AttentionTopicInputCWProxy get copyWith =>
      _$AttentionTopicInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AttentionTopicInput _$AttentionTopicInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AttentionTopicInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'name']);
      final val = AttentionTopicInput(
        id: $checkedConvert('id', (v) => v as String),
        name: $checkedConvert('name', (v) => v as String),
        description: $checkedConvert('description', (v) => v as String?),
        gdeltQuery: $checkedConvert('gdeltQuery', (v) => v as String?),
        wikiArticles: $checkedConvert(
          'wikiArticles',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => AttentionTopicInputWikiArticlesInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        keywords: $checkedConvert(
          'keywords',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        regionCodes: $checkedConvert(
          'regionCodes',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        themes: $checkedConvert(
          'themes',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        enabled: $checkedConvert('enabled', (v) => v as bool?),
        social: $checkedConvert(
          'social',
          (v) => v == null
              ? null
              : AttentionTopicSocial.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$AttentionTopicInputToJson(
  AttentionTopicInput instance,
) => <String, dynamic>{
  'id': instance.id,
  'name': instance.name,
  'description': ?instance.description,
  'gdeltQuery': ?instance.gdeltQuery,
  'wikiArticles': ?instance.wikiArticles?.map((e) => e.toJson()).toList(),
  'keywords': ?instance.keywords,
  'regionCodes': ?instance.regionCodes,
  'themes': ?instance.themes,
  'enabled': ?instance.enabled,
  'social': ?instance.social?.toJson(),
};
