// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'attention_topic.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AttentionTopicCWProxy {
  AttentionTopic id(String id);

  AttentionTopic name(String name);

  AttentionTopic description(String? description);

  AttentionTopic gdeltQuery(String? gdeltQuery);

  AttentionTopic wikiArticles(
    List<AttentionTopicInputWikiArticlesInner>? wikiArticles,
  );

  AttentionTopic keywords(List<String>? keywords);

  AttentionTopic regionCodes(List<String>? regionCodes);

  AttentionTopic themes(List<String>? themes);

  AttentionTopic enabled(bool? enabled);

  AttentionTopic social(AttentionTopicSocial? social);

  AttentionTopic createdBy(String? createdBy);

  AttentionTopic createdAt(DateTime createdAt);

  AttentionTopic updatedAt(DateTime updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AttentionTopic(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AttentionTopic(...).copyWith(id: 12, name: "My name")
  /// ```
  AttentionTopic call({
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
    String? createdBy,
    DateTime createdAt,
    DateTime updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAttentionTopic.copyWith(...)` or call `instanceOfAttentionTopic.copyWith.fieldName(value)` for a single field.
class _$AttentionTopicCWProxyImpl implements _$AttentionTopicCWProxy {
  const _$AttentionTopicCWProxyImpl(this._value);

  final AttentionTopic _value;

  @override
  AttentionTopic id(String id) => call(id: id);

  @override
  AttentionTopic name(String name) => call(name: name);

  @override
  AttentionTopic description(String? description) =>
      call(description: description);

  @override
  AttentionTopic gdeltQuery(String? gdeltQuery) => call(gdeltQuery: gdeltQuery);

  @override
  AttentionTopic wikiArticles(
    List<AttentionTopicInputWikiArticlesInner>? wikiArticles,
  ) => call(wikiArticles: wikiArticles);

  @override
  AttentionTopic keywords(List<String>? keywords) => call(keywords: keywords);

  @override
  AttentionTopic regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  AttentionTopic themes(List<String>? themes) => call(themes: themes);

  @override
  AttentionTopic enabled(bool? enabled) => call(enabled: enabled);

  @override
  AttentionTopic social(AttentionTopicSocial? social) => call(social: social);

  @override
  AttentionTopic createdBy(String? createdBy) => call(createdBy: createdBy);

  @override
  AttentionTopic createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  AttentionTopic updatedAt(DateTime updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AttentionTopic(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AttentionTopic(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AttentionTopic call({
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
    Object? createdBy = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return AttentionTopic(
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
      createdBy: createdBy == const $CopyWithPlaceholder()
          ? _value.createdBy
          // ignore: cast_nullable_to_non_nullable
          : createdBy as String?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder() || updatedAt == null
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime,
    );
  }
}

extension $AttentionTopicCopyWith on AttentionTopic {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAttentionTopic.copyWith(...)` or `instanceOfAttentionTopic.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AttentionTopicCWProxy get copyWith => _$AttentionTopicCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AttentionTopic _$AttentionTopicFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AttentionTopic', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['id', 'name', 'createdAt', 'updatedAt'],
  );
  final val = AttentionTopic(
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
    createdBy: $checkedConvert('createdBy', (v) => v as String?),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    updatedAt: $checkedConvert('updatedAt', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$AttentionTopicToJson(AttentionTopic instance) =>
    <String, dynamic>{
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
      'createdBy': ?instance.createdBy,
      'createdAt': instance.createdAt.toIso8601String(),
      'updatedAt': instance.updatedAt.toIso8601String(),
    };
