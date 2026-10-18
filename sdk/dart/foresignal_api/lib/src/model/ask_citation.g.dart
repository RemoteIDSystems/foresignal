// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ask_citation.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AskCitationCWProxy {
  AskCitation id(String id);

  AskCitation seq(int? seq);

  AskCitation title(String title);

  AskCitation type(AskCitationTypeEnum? type);

  AskCitation productId(String? productId);

  AskCitation frameKey(String? frameKey);

  AskCitation run(String? run);

  AskCitation step(int? step);

  AskCitation validTime(DateTime? validTime);

  AskCitation ageMinutes(num? ageMinutes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskCitation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskCitation(...).copyWith(id: 12, name: "My name")
  /// ```
  AskCitation call({
    String id,
    int? seq,
    String title,
    AskCitationTypeEnum? type,
    String? productId,
    String? frameKey,
    String? run,
    int? step,
    DateTime? validTime,
    num? ageMinutes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAskCitation.copyWith(...)` or call `instanceOfAskCitation.copyWith.fieldName(value)` for a single field.
class _$AskCitationCWProxyImpl implements _$AskCitationCWProxy {
  const _$AskCitationCWProxyImpl(this._value);

  final AskCitation _value;

  @override
  AskCitation id(String id) => call(id: id);

  @override
  AskCitation seq(int? seq) => call(seq: seq);

  @override
  AskCitation title(String title) => call(title: title);

  @override
  AskCitation type(AskCitationTypeEnum? type) => call(type: type);

  @override
  AskCitation productId(String? productId) => call(productId: productId);

  @override
  AskCitation frameKey(String? frameKey) => call(frameKey: frameKey);

  @override
  AskCitation run(String? run) => call(run: run);

  @override
  AskCitation step(int? step) => call(step: step);

  @override
  AskCitation validTime(DateTime? validTime) => call(validTime: validTime);

  @override
  AskCitation ageMinutes(num? ageMinutes) => call(ageMinutes: ageMinutes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskCitation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskCitation(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AskCitation call({
    Object? id = const $CopyWithPlaceholder(),
    Object? seq = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? productId = const $CopyWithPlaceholder(),
    Object? frameKey = const $CopyWithPlaceholder(),
    Object? run = const $CopyWithPlaceholder(),
    Object? step = const $CopyWithPlaceholder(),
    Object? validTime = const $CopyWithPlaceholder(),
    Object? ageMinutes = const $CopyWithPlaceholder(),
  }) {
    return AskCitation(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      seq: seq == const $CopyWithPlaceholder()
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int?,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      type: type == const $CopyWithPlaceholder()
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as AskCitationTypeEnum?,
      productId: productId == const $CopyWithPlaceholder()
          ? _value.productId
          // ignore: cast_nullable_to_non_nullable
          : productId as String?,
      frameKey: frameKey == const $CopyWithPlaceholder()
          ? _value.frameKey
          // ignore: cast_nullable_to_non_nullable
          : frameKey as String?,
      run: run == const $CopyWithPlaceholder()
          ? _value.run
          // ignore: cast_nullable_to_non_nullable
          : run as String?,
      step: step == const $CopyWithPlaceholder()
          ? _value.step
          // ignore: cast_nullable_to_non_nullable
          : step as int?,
      validTime: validTime == const $CopyWithPlaceholder()
          ? _value.validTime
          // ignore: cast_nullable_to_non_nullable
          : validTime as DateTime?,
      ageMinutes: ageMinutes == const $CopyWithPlaceholder()
          ? _value.ageMinutes
          // ignore: cast_nullable_to_non_nullable
          : ageMinutes as num?,
    );
  }
}

extension $AskCitationCopyWith on AskCitation {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAskCitation.copyWith(...)` or `instanceOfAskCitation.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AskCitationCWProxy get copyWith => _$AskCitationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AskCitation _$AskCitationFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AskCitation', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'title']);
      final val = AskCitation(
        id: $checkedConvert('id', (v) => v as String),
        seq: $checkedConvert('seq', (v) => (v as num?)?.toInt()),
        title: $checkedConvert('title', (v) => v as String),
        type: $checkedConvert(
          'type',
          (v) => $enumDecodeNullable(_$AskCitationTypeEnumEnumMap, v),
        ),
        productId: $checkedConvert('productId', (v) => v as String?),
        frameKey: $checkedConvert('frameKey', (v) => v as String?),
        run: $checkedConvert('run', (v) => v as String?),
        step: $checkedConvert('step', (v) => (v as num?)?.toInt()),
        validTime: $checkedConvert(
          'validTime',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        ageMinutes: $checkedConvert('ageMinutes', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$AskCitationToJson(AskCitation instance) =>
    <String, dynamic>{
      'id': instance.id,
      'seq': ?instance.seq,
      'title': instance.title,
      'type': ?_$AskCitationTypeEnumEnumMap[instance.type],
      'productId': ?instance.productId,
      'frameKey': ?instance.frameKey,
      'run': ?instance.run,
      'step': ?instance.step,
      'validTime': ?instance.validTime?.toIso8601String(),
      'ageMinutes': ?instance.ageMinutes,
    };

const _$AskCitationTypeEnumEnumMap = {
  AskCitationTypeEnum.record: 'record',
  AskCitationTypeEnum.alert: 'alert',
  AskCitationTypeEnum.briefing: 'briefing',
  AskCitationTypeEnum.weather: 'weather',
};
