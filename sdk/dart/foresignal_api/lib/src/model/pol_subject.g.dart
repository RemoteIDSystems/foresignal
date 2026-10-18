// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pol_subject.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PolSubjectCWProxy {
  PolSubject id(String id);

  PolSubject subjectType(PolSubjectSubjectTypeEnum subjectType);

  PolSubject ref(String ref);

  PolSubject enabled(bool enabled);

  PolSubject params(Map<String, Object>? params);

  PolSubject private(bool? private);

  PolSubject createdBy(String? createdBy);

  PolSubject createdAt(DateTime createdAt);

  PolSubject lastSeq(int? lastSeq);

  PolSubject lastComputedAt(DateTime? lastComputedAt);

  PolSubject placeCount(int? placeCount);

  PolSubject stayCount(int? stayCount);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PolSubject(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PolSubject(...).copyWith(id: 12, name: "My name")
  /// ```
  PolSubject call({
    String id,
    PolSubjectSubjectTypeEnum subjectType,
    String ref,
    bool enabled,
    Map<String, Object>? params,
    bool? private,
    String? createdBy,
    DateTime createdAt,
    int? lastSeq,
    DateTime? lastComputedAt,
    int? placeCount,
    int? stayCount,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPolSubject.copyWith(...)` or call `instanceOfPolSubject.copyWith.fieldName(value)` for a single field.
class _$PolSubjectCWProxyImpl implements _$PolSubjectCWProxy {
  const _$PolSubjectCWProxyImpl(this._value);

  final PolSubject _value;

  @override
  PolSubject id(String id) => call(id: id);

  @override
  PolSubject subjectType(PolSubjectSubjectTypeEnum subjectType) =>
      call(subjectType: subjectType);

  @override
  PolSubject ref(String ref) => call(ref: ref);

  @override
  PolSubject enabled(bool enabled) => call(enabled: enabled);

  @override
  PolSubject params(Map<String, Object>? params) => call(params: params);

  @override
  PolSubject private(bool? private) => call(private: private);

  @override
  PolSubject createdBy(String? createdBy) => call(createdBy: createdBy);

  @override
  PolSubject createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  PolSubject lastSeq(int? lastSeq) => call(lastSeq: lastSeq);

  @override
  PolSubject lastComputedAt(DateTime? lastComputedAt) =>
      call(lastComputedAt: lastComputedAt);

  @override
  PolSubject placeCount(int? placeCount) => call(placeCount: placeCount);

  @override
  PolSubject stayCount(int? stayCount) => call(stayCount: stayCount);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PolSubject(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PolSubject(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PolSubject call({
    Object? id = const $CopyWithPlaceholder(),
    Object? subjectType = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? params = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? createdBy = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? lastSeq = const $CopyWithPlaceholder(),
    Object? lastComputedAt = const $CopyWithPlaceholder(),
    Object? placeCount = const $CopyWithPlaceholder(),
    Object? stayCount = const $CopyWithPlaceholder(),
  }) {
    return PolSubject(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      subjectType:
          subjectType == const $CopyWithPlaceholder() || subjectType == null
          ? _value.subjectType
          // ignore: cast_nullable_to_non_nullable
          : subjectType as PolSubjectSubjectTypeEnum,
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      params: params == const $CopyWithPlaceholder()
          ? _value.params
          // ignore: cast_nullable_to_non_nullable
          : params as Map<String, Object>?,
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
      createdBy: createdBy == const $CopyWithPlaceholder()
          ? _value.createdBy
          // ignore: cast_nullable_to_non_nullable
          : createdBy as String?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      lastSeq: lastSeq == const $CopyWithPlaceholder()
          ? _value.lastSeq
          // ignore: cast_nullable_to_non_nullable
          : lastSeq as int?,
      lastComputedAt: lastComputedAt == const $CopyWithPlaceholder()
          ? _value.lastComputedAt
          // ignore: cast_nullable_to_non_nullable
          : lastComputedAt as DateTime?,
      placeCount: placeCount == const $CopyWithPlaceholder()
          ? _value.placeCount
          // ignore: cast_nullable_to_non_nullable
          : placeCount as int?,
      stayCount: stayCount == const $CopyWithPlaceholder()
          ? _value.stayCount
          // ignore: cast_nullable_to_non_nullable
          : stayCount as int?,
    );
  }
}

extension $PolSubjectCopyWith on PolSubject {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPolSubject.copyWith(...)` or `instanceOfPolSubject.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PolSubjectCWProxy get copyWith => _$PolSubjectCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PolSubject _$PolSubjectFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PolSubject', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['id', 'subjectType', 'ref', 'enabled', 'createdAt'],
  );
  final val = PolSubject(
    id: $checkedConvert('id', (v) => v as String),
    subjectType: $checkedConvert(
      'subjectType',
      (v) => $enumDecode(_$PolSubjectSubjectTypeEnumEnumMap, v),
    ),
    ref: $checkedConvert('ref', (v) => v as String),
    enabled: $checkedConvert('enabled', (v) => v as bool),
    params: $checkedConvert(
      'params',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    private: $checkedConvert('private', (v) => v as bool?),
    createdBy: $checkedConvert('createdBy', (v) => v as String?),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    lastSeq: $checkedConvert('lastSeq', (v) => (v as num?)?.toInt()),
    lastComputedAt: $checkedConvert(
      'lastComputedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    placeCount: $checkedConvert('placeCount', (v) => (v as num?)?.toInt()),
    stayCount: $checkedConvert('stayCount', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$PolSubjectToJson(PolSubject instance) =>
    <String, dynamic>{
      'id': instance.id,
      'subjectType': _$PolSubjectSubjectTypeEnumEnumMap[instance.subjectType]!,
      'ref': instance.ref,
      'enabled': instance.enabled,
      'params': ?instance.params,
      'private': ?instance.private,
      'createdBy': ?instance.createdBy,
      'createdAt': instance.createdAt.toIso8601String(),
      'lastSeq': ?instance.lastSeq,
      'lastComputedAt': ?instance.lastComputedAt?.toIso8601String(),
      'placeCount': ?instance.placeCount,
      'stayCount': ?instance.stayCount,
    };

const _$PolSubjectSubjectTypeEnumEnumMap = {
  PolSubjectSubjectTypeEnum.record: 'record',
  PolSubjectSubjectTypeEnum.entity: 'entity',
  PolSubjectSubjectTypeEnum.watchlist: 'watchlist',
};
