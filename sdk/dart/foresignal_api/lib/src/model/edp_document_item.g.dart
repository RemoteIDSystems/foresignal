// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_document_item.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpDocumentItemCWProxy {
  EdpDocumentItem stream(String stream);

  EdpDocumentItem type(EdpDocumentItemTypeEnum type);

  EdpDocumentItem id(String id);

  EdpDocumentItem observedAt(DateTime? observedAt);

  EdpDocumentItem properties(Map<String, Object>? properties);

  EdpDocumentItem documentType(EdpDocumentItemDocumentTypeEnum documentType);

  EdpDocumentItem text(String text);

  EdpDocumentItem title(String? title);

  EdpDocumentItem url(String? url);

  EdpDocumentItem author(EdpDocumentItemAuthor? author);

  EdpDocumentItem language(String? language);

  EdpDocumentItem publishedAt(DateTime? publishedAt);

  EdpDocumentItem about(List<String>? about);

  EdpDocumentItem media(List<EdpDocumentItemMediaInner>? media);

  EdpDocumentItem geometry(EdpGeometry? geometry);

  EdpDocumentItem engagement(Map<String, num?>? engagement);

  EdpDocumentItem origin(EdpOrigin? origin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpDocumentItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpDocumentItem(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpDocumentItem call({
    String stream,
    EdpDocumentItemTypeEnum type,
    String id,
    DateTime? observedAt,
    Map<String, Object>? properties,
    EdpDocumentItemDocumentTypeEnum documentType,
    String text,
    String? title,
    String? url,
    EdpDocumentItemAuthor? author,
    String? language,
    DateTime? publishedAt,
    List<String>? about,
    List<EdpDocumentItemMediaInner>? media,
    EdpGeometry? geometry,
    Map<String, num?>? engagement,
    EdpOrigin? origin,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpDocumentItem.copyWith(...)` or call `instanceOfEdpDocumentItem.copyWith.fieldName(value)` for a single field.
class _$EdpDocumentItemCWProxyImpl implements _$EdpDocumentItemCWProxy {
  const _$EdpDocumentItemCWProxyImpl(this._value);

  final EdpDocumentItem _value;

  @override
  EdpDocumentItem stream(String stream) => call(stream: stream);

  @override
  EdpDocumentItem type(EdpDocumentItemTypeEnum type) => call(type: type);

  @override
  EdpDocumentItem id(String id) => call(id: id);

  @override
  EdpDocumentItem observedAt(DateTime? observedAt) =>
      call(observedAt: observedAt);

  @override
  EdpDocumentItem properties(Map<String, Object>? properties) =>
      call(properties: properties);

  @override
  EdpDocumentItem documentType(EdpDocumentItemDocumentTypeEnum documentType) =>
      call(documentType: documentType);

  @override
  EdpDocumentItem text(String text) => call(text: text);

  @override
  EdpDocumentItem title(String? title) => call(title: title);

  @override
  EdpDocumentItem url(String? url) => call(url: url);

  @override
  EdpDocumentItem author(EdpDocumentItemAuthor? author) => call(author: author);

  @override
  EdpDocumentItem language(String? language) => call(language: language);

  @override
  EdpDocumentItem publishedAt(DateTime? publishedAt) =>
      call(publishedAt: publishedAt);

  @override
  EdpDocumentItem about(List<String>? about) => call(about: about);

  @override
  EdpDocumentItem media(List<EdpDocumentItemMediaInner>? media) =>
      call(media: media);

  @override
  EdpDocumentItem geometry(EdpGeometry? geometry) => call(geometry: geometry);

  @override
  EdpDocumentItem engagement(Map<String, num?>? engagement) =>
      call(engagement: engagement);

  @override
  EdpDocumentItem origin(EdpOrigin? origin) => call(origin: origin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpDocumentItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpDocumentItem(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpDocumentItem call({
    Object? stream = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? properties = const $CopyWithPlaceholder(),
    Object? documentType = const $CopyWithPlaceholder(),
    Object? text = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? author = const $CopyWithPlaceholder(),
    Object? language = const $CopyWithPlaceholder(),
    Object? publishedAt = const $CopyWithPlaceholder(),
    Object? about = const $CopyWithPlaceholder(),
    Object? media = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? engagement = const $CopyWithPlaceholder(),
    Object? origin = const $CopyWithPlaceholder(),
  }) {
    return EdpDocumentItem(
      stream: stream == const $CopyWithPlaceholder() || stream == null
          ? _value.stream
          // ignore: cast_nullable_to_non_nullable
          : stream as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as EdpDocumentItemTypeEnum,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      observedAt: observedAt == const $CopyWithPlaceholder()
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime?,
      properties: properties == const $CopyWithPlaceholder()
          ? _value.properties
          // ignore: cast_nullable_to_non_nullable
          : properties as Map<String, Object>?,
      documentType:
          documentType == const $CopyWithPlaceholder() || documentType == null
          ? _value.documentType
          // ignore: cast_nullable_to_non_nullable
          : documentType as EdpDocumentItemDocumentTypeEnum,
      text: text == const $CopyWithPlaceholder() || text == null
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
      author: author == const $CopyWithPlaceholder()
          ? _value.author
          // ignore: cast_nullable_to_non_nullable
          : author as EdpDocumentItemAuthor?,
      language: language == const $CopyWithPlaceholder()
          ? _value.language
          // ignore: cast_nullable_to_non_nullable
          : language as String?,
      publishedAt: publishedAt == const $CopyWithPlaceholder()
          ? _value.publishedAt
          // ignore: cast_nullable_to_non_nullable
          : publishedAt as DateTime?,
      about: about == const $CopyWithPlaceholder()
          ? _value.about
          // ignore: cast_nullable_to_non_nullable
          : about as List<String>?,
      media: media == const $CopyWithPlaceholder()
          ? _value.media
          // ignore: cast_nullable_to_non_nullable
          : media as List<EdpDocumentItemMediaInner>?,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as EdpGeometry?,
      engagement: engagement == const $CopyWithPlaceholder()
          ? _value.engagement
          // ignore: cast_nullable_to_non_nullable
          : engagement as Map<String, num?>?,
      origin: origin == const $CopyWithPlaceholder()
          ? _value.origin
          // ignore: cast_nullable_to_non_nullable
          : origin as EdpOrigin?,
    );
  }
}

extension $EdpDocumentItemCopyWith on EdpDocumentItem {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpDocumentItem.copyWith(...)` or `instanceOfEdpDocumentItem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpDocumentItemCWProxy get copyWith => _$EdpDocumentItemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpDocumentItem _$EdpDocumentItemFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EdpDocumentItem', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['stream', 'type', 'id', 'documentType', 'text'],
  );
  final val = EdpDocumentItem(
    stream: $checkedConvert('stream', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$EdpDocumentItemTypeEnumEnumMap, v),
    ),
    id: $checkedConvert('id', (v) => v as String),
    observedAt: $checkedConvert(
      'observedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    properties: $checkedConvert(
      'properties',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    documentType: $checkedConvert(
      'documentType',
      (v) => $enumDecode(_$EdpDocumentItemDocumentTypeEnumEnumMap, v),
    ),
    text: $checkedConvert('text', (v) => v as String),
    title: $checkedConvert('title', (v) => v as String?),
    url: $checkedConvert('url', (v) => v as String?),
    author: $checkedConvert(
      'author',
      (v) => v == null
          ? null
          : EdpDocumentItemAuthor.fromJson(v as Map<String, dynamic>),
    ),
    language: $checkedConvert('language', (v) => v as String?),
    publishedAt: $checkedConvert(
      'publishedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    about: $checkedConvert(
      'about',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    media: $checkedConvert(
      'media',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) =>
                EdpDocumentItemMediaInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    geometry: $checkedConvert(
      'geometry',
      (v) => v == null ? null : EdpGeometry.fromJson(v as Map<String, dynamic>),
    ),
    engagement: $checkedConvert(
      'engagement',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as num?)),
    ),
    origin: $checkedConvert(
      'origin',
      (v) => v == null ? null : EdpOrigin.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$EdpDocumentItemToJson(EdpDocumentItem instance) =>
    <String, dynamic>{
      'stream': instance.stream,
      'type': _$EdpDocumentItemTypeEnumEnumMap[instance.type]!,
      'id': instance.id,
      'observedAt': ?instance.observedAt?.toIso8601String(),
      'properties': ?instance.properties,
      'documentType':
          _$EdpDocumentItemDocumentTypeEnumEnumMap[instance.documentType]!,
      'text': instance.text,
      'title': ?instance.title,
      'url': ?instance.url,
      'author': ?instance.author?.toJson(),
      'language': ?instance.language,
      'publishedAt': ?instance.publishedAt?.toIso8601String(),
      'about': ?instance.about,
      'media': ?instance.media?.map((e) => e.toJson()).toList(),
      'geometry': ?instance.geometry?.toJson(),
      'engagement': ?instance.engagement,
      'origin': ?instance.origin?.toJson(),
    };

const _$EdpDocumentItemTypeEnumEnumMap = {
  EdpDocumentItemTypeEnum.document: 'document',
};

const _$EdpDocumentItemDocumentTypeEnumEnumMap = {
  EdpDocumentItemDocumentTypeEnum.socialPost: 'social-post',
  EdpDocumentItemDocumentTypeEnum.article: 'article',
  EdpDocumentItemDocumentTypeEnum.report: 'report',
  EdpDocumentItemDocumentTypeEnum.transcript: 'transcript',
  EdpDocumentItemDocumentTypeEnum.note: 'note',
};
