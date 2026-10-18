//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'taxii_discovery.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TaxiiDiscovery {
  /// Returns a new [TaxiiDiscovery] instance.
  TaxiiDiscovery({

    required  this.title,

     this.description,

     this.contact,

     this.default_,

     this.apiRoots,
  });

  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'contact',
    required: false,
    includeIfNull: false,
  )


  final String? contact;



  @JsonKey(
    
    name: r'default',
    required: false,
    includeIfNull: false,
  )


  final String? default_;



  @JsonKey(
    
    name: r'api_roots',
    required: false,
    includeIfNull: false,
  )


  final List<String>? apiRoots;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TaxiiDiscovery &&
      other.title == title &&
      other.description == description &&
      other.contact == contact &&
      other.default_ == default_ &&
      other.apiRoots == apiRoots;

    @override
    int get hashCode =>
        title.hashCode +
        (description == null ? 0 : description.hashCode) +
        (contact == null ? 0 : contact.hashCode) +
        (default_ == null ? 0 : default_.hashCode) +
        apiRoots.hashCode;

  factory TaxiiDiscovery.fromJson(Map<String, dynamic> json) => _$TaxiiDiscoveryFromJson(json);

  Map<String, dynamic> toJson() => _$TaxiiDiscoveryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

