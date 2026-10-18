// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'outlook_window.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OutlookWindowCWProxy {
  OutlookWindow index(int index);

  OutlookWindow windowStart(DateTime windowStart);

  OutlookWindow windowEnd(DateTime windowEnd);

  OutlookWindow quantiles(Quantiles quantiles);

  OutlookWindow questionId(String? questionId);

  OutlookWindow status(OutlookWindowStatusEnum status);

  OutlookWindow actual(int? actual);

  OutlookWindow withinP10P90(bool? withinP10P90);

  OutlookWindow exceedance(OutlookWindowExceedanceEnum? exceedance);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OutlookWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OutlookWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  OutlookWindow call({
    int index,
    DateTime windowStart,
    DateTime windowEnd,
    Quantiles quantiles,
    String? questionId,
    OutlookWindowStatusEnum status,
    int? actual,
    bool? withinP10P90,
    OutlookWindowExceedanceEnum? exceedance,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOutlookWindow.copyWith(...)` or call `instanceOfOutlookWindow.copyWith.fieldName(value)` for a single field.
class _$OutlookWindowCWProxyImpl implements _$OutlookWindowCWProxy {
  const _$OutlookWindowCWProxyImpl(this._value);

  final OutlookWindow _value;

  @override
  OutlookWindow index(int index) => call(index: index);

  @override
  OutlookWindow windowStart(DateTime windowStart) =>
      call(windowStart: windowStart);

  @override
  OutlookWindow windowEnd(DateTime windowEnd) => call(windowEnd: windowEnd);

  @override
  OutlookWindow quantiles(Quantiles quantiles) => call(quantiles: quantiles);

  @override
  OutlookWindow questionId(String? questionId) => call(questionId: questionId);

  @override
  OutlookWindow status(OutlookWindowStatusEnum status) => call(status: status);

  @override
  OutlookWindow actual(int? actual) => call(actual: actual);

  @override
  OutlookWindow withinP10P90(bool? withinP10P90) =>
      call(withinP10P90: withinP10P90);

  @override
  OutlookWindow exceedance(OutlookWindowExceedanceEnum? exceedance) =>
      call(exceedance: exceedance);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OutlookWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OutlookWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OutlookWindow call({
    Object? index = const $CopyWithPlaceholder(),
    Object? windowStart = const $CopyWithPlaceholder(),
    Object? windowEnd = const $CopyWithPlaceholder(),
    Object? quantiles = const $CopyWithPlaceholder(),
    Object? questionId = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? actual = const $CopyWithPlaceholder(),
    Object? withinP10P90 = const $CopyWithPlaceholder(),
    Object? exceedance = const $CopyWithPlaceholder(),
  }) {
    return OutlookWindow(
      index: index == const $CopyWithPlaceholder() || index == null
          ? _value.index
          // ignore: cast_nullable_to_non_nullable
          : index as int,
      windowStart:
          windowStart == const $CopyWithPlaceholder() || windowStart == null
          ? _value.windowStart
          // ignore: cast_nullable_to_non_nullable
          : windowStart as DateTime,
      windowEnd: windowEnd == const $CopyWithPlaceholder() || windowEnd == null
          ? _value.windowEnd
          // ignore: cast_nullable_to_non_nullable
          : windowEnd as DateTime,
      quantiles: quantiles == const $CopyWithPlaceholder() || quantiles == null
          ? _value.quantiles
          // ignore: cast_nullable_to_non_nullable
          : quantiles as Quantiles,
      questionId: questionId == const $CopyWithPlaceholder()
          ? _value.questionId
          // ignore: cast_nullable_to_non_nullable
          : questionId as String?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as OutlookWindowStatusEnum,
      actual: actual == const $CopyWithPlaceholder()
          ? _value.actual
          // ignore: cast_nullable_to_non_nullable
          : actual as int?,
      withinP10P90: withinP10P90 == const $CopyWithPlaceholder()
          ? _value.withinP10P90
          // ignore: cast_nullable_to_non_nullable
          : withinP10P90 as bool?,
      exceedance: exceedance == const $CopyWithPlaceholder()
          ? _value.exceedance
          // ignore: cast_nullable_to_non_nullable
          : exceedance as OutlookWindowExceedanceEnum?,
    );
  }
}

extension $OutlookWindowCopyWith on OutlookWindow {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOutlookWindow.copyWith(...)` or `instanceOfOutlookWindow.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OutlookWindowCWProxy get copyWith => _$OutlookWindowCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OutlookWindow _$OutlookWindowFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OutlookWindow', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'index',
          'windowStart',
          'windowEnd',
          'quantiles',
          'status',
        ],
      );
      final val = OutlookWindow(
        index: $checkedConvert('index', (v) => (v as num).toInt()),
        windowStart: $checkedConvert(
          'windowStart',
          (v) => DateTime.parse(v as String),
        ),
        windowEnd: $checkedConvert(
          'windowEnd',
          (v) => DateTime.parse(v as String),
        ),
        quantiles: $checkedConvert(
          'quantiles',
          (v) => Quantiles.fromJson(v as Map<String, dynamic>),
        ),
        questionId: $checkedConvert('questionId', (v) => v as String?),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$OutlookWindowStatusEnumEnumMap, v),
        ),
        actual: $checkedConvert('actual', (v) => (v as num?)?.toInt()),
        withinP10P90: $checkedConvert('withinP10P90', (v) => v as bool?),
        exceedance: $checkedConvert(
          'exceedance',
          (v) => $enumDecodeNullable(_$OutlookWindowExceedanceEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$OutlookWindowToJson(OutlookWindow instance) =>
    <String, dynamic>{
      'index': instance.index,
      'windowStart': instance.windowStart.toIso8601String(),
      'windowEnd': instance.windowEnd.toIso8601String(),
      'quantiles': instance.quantiles.toJson(),
      'questionId': ?instance.questionId,
      'status': _$OutlookWindowStatusEnumEnumMap[instance.status]!,
      'actual': ?instance.actual,
      'withinP10P90': ?instance.withinP10P90,
      'exceedance': ?_$OutlookWindowExceedanceEnumEnumMap[instance.exceedance],
    };

const _$OutlookWindowStatusEnumEnumMap = {
  OutlookWindowStatusEnum.open: 'open',
  OutlookWindowStatusEnum.resolved: 'resolved',
  OutlookWindowStatusEnum.annulled: 'annulled',
};

const _$OutlookWindowExceedanceEnumEnumMap = {
  OutlookWindowExceedanceEnum.aboveP95: 'above-p95',
  OutlookWindowExceedanceEnum.belowP05: 'below-p05',
};
