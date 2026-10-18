//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'exchange_package_input_extract.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExchangePackageInputExtract {
  /// Returns a new [ExchangePackageInputExtract] instance.
  ExchangePackageInputExtract({

     this.from,

     this.to,

     this.layers,
  });

  @JsonKey(
    
    name: r'from',
    required: false,
    includeIfNull: false,
  )


  final DateTime? from;



  @JsonKey(
    
    name: r'to',
    required: false,
    includeIfNull: false,
  )


  final DateTime? to;



  @JsonKey(
    
    name: r'layers',
    required: false,
    includeIfNull: false,
  )


  final List<String>? layers;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExchangePackageInputExtract &&
      other.from == from &&
      other.to == to &&
      other.layers == layers;

    @override
    int get hashCode =>
        (from == null ? 0 : from.hashCode) +
        (to == null ? 0 : to.hashCode) +
        layers.hashCode;

  factory ExchangePackageInputExtract.fromJson(Map<String, dynamic> json) => _$ExchangePackageInputExtractFromJson(json);

  Map<String, dynamic> toJson() => _$ExchangePackageInputExtractToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

