// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'attention_topic_input_wiki_articles_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AttentionTopicInputWikiArticlesInnerCWProxy {
  AttentionTopicInputWikiArticlesInner project(String project);

  AttentionTopicInputWikiArticlesInner article(String article);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AttentionTopicInputWikiArticlesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AttentionTopicInputWikiArticlesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  AttentionTopicInputWikiArticlesInner call({String project, String article});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAttentionTopicInputWikiArticlesInner.copyWith(...)` or call `instanceOfAttentionTopicInputWikiArticlesInner.copyWith.fieldName(value)` for a single field.
class _$AttentionTopicInputWikiArticlesInnerCWProxyImpl
    implements _$AttentionTopicInputWikiArticlesInnerCWProxy {
  const _$AttentionTopicInputWikiArticlesInnerCWProxyImpl(this._value);

  final AttentionTopicInputWikiArticlesInner _value;

  @override
  AttentionTopicInputWikiArticlesInner project(String project) =>
      call(project: project);

  @override
  AttentionTopicInputWikiArticlesInner article(String article) =>
      call(article: article);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AttentionTopicInputWikiArticlesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AttentionTopicInputWikiArticlesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AttentionTopicInputWikiArticlesInner call({
    Object? project = const $CopyWithPlaceholder(),
    Object? article = const $CopyWithPlaceholder(),
  }) {
    return AttentionTopicInputWikiArticlesInner(
      project: project == const $CopyWithPlaceholder() || project == null
          ? _value.project
          // ignore: cast_nullable_to_non_nullable
          : project as String,
      article: article == const $CopyWithPlaceholder() || article == null
          ? _value.article
          // ignore: cast_nullable_to_non_nullable
          : article as String,
    );
  }
}

extension $AttentionTopicInputWikiArticlesInnerCopyWith
    on AttentionTopicInputWikiArticlesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAttentionTopicInputWikiArticlesInner.copyWith(...)` or `instanceOfAttentionTopicInputWikiArticlesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AttentionTopicInputWikiArticlesInnerCWProxy get copyWith =>
      _$AttentionTopicInputWikiArticlesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AttentionTopicInputWikiArticlesInner
_$AttentionTopicInputWikiArticlesInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AttentionTopicInputWikiArticlesInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['project', 'article']);
      final val = AttentionTopicInputWikiArticlesInner(
        project: $checkedConvert('project', (v) => v as String),
        article: $checkedConvert('article', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$AttentionTopicInputWikiArticlesInnerToJson(
  AttentionTopicInputWikiArticlesInner instance,
) => <String, dynamic>{
  'project': instance.project,
  'article': instance.article,
};
