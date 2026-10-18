// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'backtest_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BacktestRequestCWProxy {
  BacktestRequest type(BacktestRequestTypeEnum type);

  BacktestRequest targetId(String? targetId);

  BacktestRequest definition(Map<String, Object>? definition);

  BacktestRequest from(DateTime? from);

  BacktestRequest to(DateTime? to);

  BacktestRequest fromSeq(int? fromSeq);

  BacktestRequest toSeq(int? toSeq);

  BacktestRequest variants(List<Map<String, Object>>? variants);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BacktestRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BacktestRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  BacktestRequest call({
    BacktestRequestTypeEnum type,
    String? targetId,
    Map<String, Object>? definition,
    DateTime? from,
    DateTime? to,
    int? fromSeq,
    int? toSeq,
    List<Map<String, Object>>? variants,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBacktestRequest.copyWith(...)` or call `instanceOfBacktestRequest.copyWith.fieldName(value)` for a single field.
class _$BacktestRequestCWProxyImpl implements _$BacktestRequestCWProxy {
  const _$BacktestRequestCWProxyImpl(this._value);

  final BacktestRequest _value;

  @override
  BacktestRequest type(BacktestRequestTypeEnum type) => call(type: type);

  @override
  BacktestRequest targetId(String? targetId) => call(targetId: targetId);

  @override
  BacktestRequest definition(Map<String, Object>? definition) =>
      call(definition: definition);

  @override
  BacktestRequest from(DateTime? from) => call(from: from);

  @override
  BacktestRequest to(DateTime? to) => call(to: to);

  @override
  BacktestRequest fromSeq(int? fromSeq) => call(fromSeq: fromSeq);

  @override
  BacktestRequest toSeq(int? toSeq) => call(toSeq: toSeq);

  @override
  BacktestRequest variants(List<Map<String, Object>>? variants) =>
      call(variants: variants);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BacktestRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BacktestRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BacktestRequest call({
    Object? type = const $CopyWithPlaceholder(),
    Object? targetId = const $CopyWithPlaceholder(),
    Object? definition = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? fromSeq = const $CopyWithPlaceholder(),
    Object? toSeq = const $CopyWithPlaceholder(),
    Object? variants = const $CopyWithPlaceholder(),
  }) {
    return BacktestRequest(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as BacktestRequestTypeEnum,
      targetId: targetId == const $CopyWithPlaceholder()
          ? _value.targetId
          // ignore: cast_nullable_to_non_nullable
          : targetId as String?,
      definition: definition == const $CopyWithPlaceholder()
          ? _value.definition
          // ignore: cast_nullable_to_non_nullable
          : definition as Map<String, Object>?,
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime?,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime?,
      fromSeq: fromSeq == const $CopyWithPlaceholder()
          ? _value.fromSeq
          // ignore: cast_nullable_to_non_nullable
          : fromSeq as int?,
      toSeq: toSeq == const $CopyWithPlaceholder()
          ? _value.toSeq
          // ignore: cast_nullable_to_non_nullable
          : toSeq as int?,
      variants: variants == const $CopyWithPlaceholder()
          ? _value.variants
          // ignore: cast_nullable_to_non_nullable
          : variants as List<Map<String, Object>>?,
    );
  }
}

extension $BacktestRequestCopyWith on BacktestRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBacktestRequest.copyWith(...)` or `instanceOfBacktestRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BacktestRequestCWProxy get copyWith => _$BacktestRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BacktestRequest _$BacktestRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BacktestRequest', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type']);
      final val = BacktestRequest(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$BacktestRequestTypeEnumEnumMap, v),
        ),
        targetId: $checkedConvert('targetId', (v) => v as String?),
        definition: $checkedConvert(
          'definition',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
        from: $checkedConvert(
          'from',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        to: $checkedConvert(
          'to',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        fromSeq: $checkedConvert('fromSeq', (v) => (v as num?)?.toInt()),
        toSeq: $checkedConvert('toSeq', (v) => (v as num?)?.toInt()),
        variants: $checkedConvert(
          'variants',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => (e as Map<String, dynamic>).map(
                  (k, e) => MapEntry(k, e as Object),
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$BacktestRequestToJson(BacktestRequest instance) =>
    <String, dynamic>{
      'type': _$BacktestRequestTypeEnumEnumMap[instance.type]!,
      'targetId': ?instance.targetId,
      'definition': ?instance.definition,
      'from': ?instance.from?.toIso8601String(),
      'to': ?instance.to?.toIso8601String(),
      'fromSeq': ?instance.fromSeq,
      'toSeq': ?instance.toSeq,
      'variants': ?instance.variants,
    };

const _$BacktestRequestTypeEnumEnumMap = {
  BacktestRequestTypeEnum.rule: 'rule',
  BacktestRequestTypeEnum.recipe: 'recipe',
  BacktestRequestTypeEnum.precursor: 'precursor',
  BacktestRequestTypeEnum.quality: 'quality',
};
