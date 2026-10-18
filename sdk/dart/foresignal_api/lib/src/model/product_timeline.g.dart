// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'product_timeline.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProductTimelineCWProxy {
  ProductTimeline mode(ProductTimelineModeEnum mode);

  ProductTimeline products(List<String> products);

  ProductTimeline from(DateTime? from);

  ProductTimeline to(DateTime? to);

  ProductTimeline runs(Map<String, String>? runs);

  ProductTimeline nowIndex(int? nowIndex);

  ProductTimeline ticks(List<TimelineTick> ticks);

  ProductTimeline serverTime(DateTime serverTime);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductTimeline(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductTimeline(...).copyWith(id: 12, name: "My name")
  /// ```
  ProductTimeline call({
    ProductTimelineModeEnum mode,
    List<String> products,
    DateTime? from,
    DateTime? to,
    Map<String, String>? runs,
    int? nowIndex,
    List<TimelineTick> ticks,
    DateTime serverTime,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProductTimeline.copyWith(...)` or call `instanceOfProductTimeline.copyWith.fieldName(value)` for a single field.
class _$ProductTimelineCWProxyImpl implements _$ProductTimelineCWProxy {
  const _$ProductTimelineCWProxyImpl(this._value);

  final ProductTimeline _value;

  @override
  ProductTimeline mode(ProductTimelineModeEnum mode) => call(mode: mode);

  @override
  ProductTimeline products(List<String> products) => call(products: products);

  @override
  ProductTimeline from(DateTime? from) => call(from: from);

  @override
  ProductTimeline to(DateTime? to) => call(to: to);

  @override
  ProductTimeline runs(Map<String, String>? runs) => call(runs: runs);

  @override
  ProductTimeline nowIndex(int? nowIndex) => call(nowIndex: nowIndex);

  @override
  ProductTimeline ticks(List<TimelineTick> ticks) => call(ticks: ticks);

  @override
  ProductTimeline serverTime(DateTime serverTime) =>
      call(serverTime: serverTime);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductTimeline(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductTimeline(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProductTimeline call({
    Object? mode = const $CopyWithPlaceholder(),
    Object? products = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? runs = const $CopyWithPlaceholder(),
    Object? nowIndex = const $CopyWithPlaceholder(),
    Object? ticks = const $CopyWithPlaceholder(),
    Object? serverTime = const $CopyWithPlaceholder(),
  }) {
    return ProductTimeline(
      mode: mode == const $CopyWithPlaceholder() || mode == null
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as ProductTimelineModeEnum,
      products: products == const $CopyWithPlaceholder() || products == null
          ? _value.products
          // ignore: cast_nullable_to_non_nullable
          : products as List<String>,
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime?,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime?,
      runs: runs == const $CopyWithPlaceholder()
          ? _value.runs
          // ignore: cast_nullable_to_non_nullable
          : runs as Map<String, String>?,
      nowIndex: nowIndex == const $CopyWithPlaceholder()
          ? _value.nowIndex
          // ignore: cast_nullable_to_non_nullable
          : nowIndex as int?,
      ticks: ticks == const $CopyWithPlaceholder() || ticks == null
          ? _value.ticks
          // ignore: cast_nullable_to_non_nullable
          : ticks as List<TimelineTick>,
      serverTime:
          serverTime == const $CopyWithPlaceholder() || serverTime == null
          ? _value.serverTime
          // ignore: cast_nullable_to_non_nullable
          : serverTime as DateTime,
    );
  }
}

extension $ProductTimelineCopyWith on ProductTimeline {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProductTimeline.copyWith(...)` or `instanceOfProductTimeline.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProductTimelineCWProxy get copyWith => _$ProductTimelineCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProductTimeline _$ProductTimelineFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ProductTimeline', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['mode', 'products', 'ticks', 'serverTime'],
      );
      final val = ProductTimeline(
        mode: $checkedConvert(
          'mode',
          (v) => $enumDecode(_$ProductTimelineModeEnumEnumMap, v),
        ),
        products: $checkedConvert(
          'products',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        from: $checkedConvert(
          'from',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        to: $checkedConvert(
          'to',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        runs: $checkedConvert(
          'runs',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as String),
          ),
        ),
        nowIndex: $checkedConvert('nowIndex', (v) => (v as num?)?.toInt()),
        ticks: $checkedConvert(
          'ticks',
          (v) => (v as List<dynamic>)
              .map((e) => TimelineTick.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        serverTime: $checkedConvert(
          'serverTime',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ProductTimelineToJson(ProductTimeline instance) =>
    <String, dynamic>{
      'mode': _$ProductTimelineModeEnumEnumMap[instance.mode]!,
      'products': instance.products,
      'from': ?instance.from?.toIso8601String(),
      'to': ?instance.to?.toIso8601String(),
      'runs': ?instance.runs,
      'nowIndex': ?instance.nowIndex,
      'ticks': instance.ticks.map((e) => e.toJson()).toList(),
      'serverTime': instance.serverTime.toIso8601String(),
    };

const _$ProductTimelineModeEnumEnumMap = {
  ProductTimelineModeEnum.products: 'products',
  ProductTimelineModeEnum.seamless: 'seamless',
};
