// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'digest_notable_item.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DigestNotableItemCWProxy {
  DigestNotableItem id(String id);

  DigestNotableItem source_(String? source_);

  DigestNotableItem externalId(String? externalId);

  DigestNotableItem kind(String? kind);

  DigestNotableItem seq(int seq);

  DigestNotableItem title(String? title);

  DigestNotableItem live(bool? live);

  DigestNotableItem observedAt(DateTime? observedAt);

  DigestNotableItem effectiveTime(DateTime? effectiveTime);

  DigestNotableItem rankValue(Object? rankValue);

  DigestNotableItem link(String? link);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestNotableItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestNotableItem(...).copyWith(id: 12, name: "My name")
  /// ```
  DigestNotableItem call({
    String id,
    String? source_,
    String? externalId,
    String? kind,
    int seq,
    String? title,
    bool? live,
    DateTime? observedAt,
    DateTime? effectiveTime,
    Object? rankValue,
    String? link,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDigestNotableItem.copyWith(...)` or call `instanceOfDigestNotableItem.copyWith.fieldName(value)` for a single field.
class _$DigestNotableItemCWProxyImpl implements _$DigestNotableItemCWProxy {
  const _$DigestNotableItemCWProxyImpl(this._value);

  final DigestNotableItem _value;

  @override
  DigestNotableItem id(String id) => call(id: id);

  @override
  DigestNotableItem source_(String? source_) => call(source_: source_);

  @override
  DigestNotableItem externalId(String? externalId) =>
      call(externalId: externalId);

  @override
  DigestNotableItem kind(String? kind) => call(kind: kind);

  @override
  DigestNotableItem seq(int seq) => call(seq: seq);

  @override
  DigestNotableItem title(String? title) => call(title: title);

  @override
  DigestNotableItem live(bool? live) => call(live: live);

  @override
  DigestNotableItem observedAt(DateTime? observedAt) =>
      call(observedAt: observedAt);

  @override
  DigestNotableItem effectiveTime(DateTime? effectiveTime) =>
      call(effectiveTime: effectiveTime);

  @override
  DigestNotableItem rankValue(Object? rankValue) => call(rankValue: rankValue);

  @override
  DigestNotableItem link(String? link) => call(link: link);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestNotableItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestNotableItem(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DigestNotableItem call({
    Object? id = const $CopyWithPlaceholder(),
    Object? source_ = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? seq = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? live = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? effectiveTime = const $CopyWithPlaceholder(),
    Object? rankValue = const $CopyWithPlaceholder(),
    Object? link = const $CopyWithPlaceholder(),
  }) {
    return DigestNotableItem(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      source_: source_ == const $CopyWithPlaceholder()
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as String?,
      externalId: externalId == const $CopyWithPlaceholder()
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String?,
      kind: kind == const $CopyWithPlaceholder()
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String?,
      seq: seq == const $CopyWithPlaceholder() || seq == null
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      live: live == const $CopyWithPlaceholder()
          ? _value.live
          // ignore: cast_nullable_to_non_nullable
          : live as bool?,
      observedAt: observedAt == const $CopyWithPlaceholder()
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime?,
      effectiveTime: effectiveTime == const $CopyWithPlaceholder()
          ? _value.effectiveTime
          // ignore: cast_nullable_to_non_nullable
          : effectiveTime as DateTime?,
      rankValue: rankValue == const $CopyWithPlaceholder()
          ? _value.rankValue
          // ignore: cast_nullable_to_non_nullable
          : rankValue as Object?,
      link: link == const $CopyWithPlaceholder()
          ? _value.link
          // ignore: cast_nullable_to_non_nullable
          : link as String?,
    );
  }
}

extension $DigestNotableItemCopyWith on DigestNotableItem {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDigestNotableItem.copyWith(...)` or `instanceOfDigestNotableItem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DigestNotableItemCWProxy get copyWith =>
      _$DigestNotableItemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DigestNotableItem _$DigestNotableItemFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DigestNotableItem', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'seq']);
      final val = DigestNotableItem(
        id: $checkedConvert('id', (v) => v as String),
        source_: $checkedConvert('source', (v) => v as String?),
        externalId: $checkedConvert('externalId', (v) => v as String?),
        kind: $checkedConvert('kind', (v) => v as String?),
        seq: $checkedConvert('seq', (v) => (v as num).toInt()),
        title: $checkedConvert('title', (v) => v as String?),
        live: $checkedConvert('live', (v) => v as bool?),
        observedAt: $checkedConvert(
          'observedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        effectiveTime: $checkedConvert(
          'effectiveTime',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        rankValue: $checkedConvert('rankValue', (v) => v),
        link: $checkedConvert('link', (v) => v as String?),
      );
      return val;
    }, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$DigestNotableItemToJson(DigestNotableItem instance) =>
    <String, dynamic>{
      'id': instance.id,
      'source': ?instance.source_,
      'externalId': ?instance.externalId,
      'kind': ?instance.kind,
      'seq': instance.seq,
      'title': ?instance.title,
      'live': ?instance.live,
      'observedAt': ?instance.observedAt?.toIso8601String(),
      'effectiveTime': ?instance.effectiveTime?.toIso8601String(),
      'rankValue': ?instance.rankValue,
      'link': ?instance.link,
    };
