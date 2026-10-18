//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'product_temporal_runs_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProductTemporalRunsInner {
  /// Returns a new [ProductTemporalRunsInner] instance.
  ProductTemporalRunsInner({

    required  this.run,

    required  this.status,

     this.steps,

     this.validFrom,

     this.validTo,
  });

      /// Run reference time (ISO-8601), usable as `run=`
  @JsonKey(
    
    name: r'run',
    required: true,
    includeIfNull: false,
  )


  final String run;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ProductTemporalRunsInnerStatusEnum status;



      /// Forecast steps (hours) present
  @JsonKey(
    
    name: r'steps',
    required: false,
    includeIfNull: false,
  )


  final List<int>? steps;



  @JsonKey(
    
    name: r'validFrom',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validFrom;



  @JsonKey(
    
    name: r'validTo',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validTo;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProductTemporalRunsInner &&
      other.run == run &&
      other.status == status &&
      other.steps == steps &&
      other.validFrom == validFrom &&
      other.validTo == validTo;

    @override
    int get hashCode =>
        run.hashCode +
        status.hashCode +
        steps.hashCode +
        validFrom.hashCode +
        validTo.hashCode;

  factory ProductTemporalRunsInner.fromJson(Map<String, dynamic> json) => _$ProductTemporalRunsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ProductTemporalRunsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ProductTemporalRunsInnerStatusEnum {
@JsonValue(r'complete')
complete(r'complete'),
@JsonValue(r'partial')
partial(r'partial');

const ProductTemporalRunsInnerStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


